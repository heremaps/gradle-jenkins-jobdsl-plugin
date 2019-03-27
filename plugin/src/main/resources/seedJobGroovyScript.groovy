/*
 * Copyright (C) 2016-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

import groovy.xml.XmlUtil
import hudson.FilePath
import hudson.model.Item
import hudson.model.ItemGroup
import hudson.model.ModifiableViewGroup
import hudson.model.View
import jenkins.model.Jenkins
import java.nio.file.Files
import javax.xml.transform.stream.StreamSource
import org.custommonkey.xmlunit.Diff
import org.custommonkey.xmlunit.XMLUnit

createdItems = 0
createdViews = 0
updatedItems = 0
updatedViews = 0
totalItems = 0
totalViews = 0
totalFailures = 0

def updateItem(Item item, String name, FilePath file) {
    println "  Update item ${name}"

    file.read().withCloseable { xmlStream ->
        try {
            String oldConfig = item.getConfigFile().asString()
            String newConfig = file.read().getText('UTF-8')

            Diff diff = XMLUnit.compareXML(oldConfig, newConfig)
            if (diff.identical()) {
                println "  Item ${name} did not change"
                return
            }

            // Retain folder credentials if present by copying from existing job XML
            def folderCredentialsProviderKey = 'com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider_-FolderCredentialsProperty'
            def existingXmlParsed = new XmlParser().parseText(oldConfig)
            def newXmlParsed = new XmlParser().parseText(newConfig)
            if (existingXmlParsed.properties?."${folderCredentialsProviderKey}".size() > 0 &&
                    newXmlParsed.properties?."${folderCredentialsProviderKey}".size() == 0) {
                // Credentials found on existing job, so copy them over to new job XML
                def node = existingXmlParsed.properties[0]."${folderCredentialsProviderKey}"[0] as Node
                newXmlParsed.properties[0].children().add(node)

                // Close new job XML file and instead read from new XML + copied properties.
                xmlStream.close()
                xmlStream = new ByteArrayInputStream(XmlUtil.serialize(newXmlParsed).getBytes('UTF-8'))
            }
        } catch (Exception e) {
            println "  WARNING: Could not create XML diff for ${name}"
        }

        item.updateByXml(new StreamSource(xmlStream))
    }
    updatedItems++
}

def createItem(ItemGroup parent, String name, FilePath file) {
    println "  Create item ${name}"
    if (parent instanceof ModifiableViewGroup) {
        file.read().withCloseable { xmlStream ->
            parent.createProjectFromXML(name, xmlStream)
        }
        createdItems++
    } else if (parent == null) {
        throw new RuntimeException("""\
            Parent is null. This happens when the parent folder could not get created.
            For example, a possible cause is that a job with the name of the parent folder already exists.
            To solve that case, rename the job or the parent folder.""".stripIndent())
    } else {
        throw new RuntimeException("Parent is not a folder type ModifiableViewGroup but of type '${parent.class}'")
    }
}

def updateView(View view, String name, FilePath file) {
    println "  Update view ${name}"
    file.read().withCloseable { xmlStream ->
        view.updateByXml(new StreamSource(xmlStream))
    }
    updatedViews++
}

def createView(ItemGroup parent, String name, FilePath file) {
    println "  Create view ${name}"
    if (parent instanceof ModifiableViewGroup) {
        file.read().withCloseable { xmlStream ->
            ((ModifiableViewGroup) parent).addView(View.createViewFromXML(name, xmlStream))
        }
        createdViews++
    } else if (parent == null) {
        throw new RuntimeException("""\
            Parent is null. This happens when the parent folder could not get created.
            For example, a possible cause is that a job with the name of the parent folder already exists.
            To solve that case, rename the job or the parent folder.""".stripIndent())
    } else {
        throw new RuntimeException("Parent is not a folder type ModifiableViewGroup but of type '${parent.class}'")
    }
}

def findParentItem(String fullName) {
    Jenkins jenkins = Jenkins.getInstance()
    int i = fullName.lastIndexOf('/')
    switch (i) {
        case -1:
        case 0:
            return jenkins
        default:
            def parentName = fullName.substring(0, i)
            Item item = jenkins.getItemByFullName(parentName)

            if (item == null) {
                println "  WARNING: parent folder ${parentName} not found"
                return null
            } else if (item instanceof ItemGroup) {
                return item
            } else {
                println "  WARNING: parent ${parentName} is not a folder but of type '${item.class}''"
            }
    }
}

def updateJobs(FilePath filePath, String namePrefix = '') {
    List<FilePath> directories = []
    filePath.list().each { file ->
        if (file.directory) {
            directories << file
        } else if (file.name.endsWith('.xml')) {
            def name = file.name[0..-5]
            Jenkins.checkGoodName(name)
            def fullName = "${namePrefix}${name}"

            println "Processing ${fullName}"
            def parent = findParentItem(fullName)

            // Detect whether XML is for a view or job by reading first line of the XML
            def firstLine
            new BufferedReader(new InputStreamReader(file.read())).withCloseable { xmlReader ->
                firstLine = xmlReader.readLine()
                if (firstLine.contains('?xml')) {
                    firstLine = xmlReader.readLine()
                }
            }

            // Handle view
            if (firstLine.contains('View')) {
                totalViews++
                def view = parent.getView(name)
                try {
                    if (view) {
                        updateView(view, name, file)
                    } else {
                        createView(parent, name, file)
                    }
                } catch (Exception ex) {
                    def action = view ? "update" : "create"
                    println "  ERROR: Could not ${action} ${fullName}: ${ex.message}"
                    totalFailures++
                }

            // Handle job (freestyle or pipeline job)
            } else {
                totalItems++
                def item = Jenkins.getInstance().getItemByFullName(fullName)
                try {
                    if (item) {
                        updateItem(item, name, file)
                    } else {
                        createItem(parent, name, file)
                    }
                } catch (Exception ex) {
                    def action = item ? "update" : "create"
                    println "  ERROR: Could not ${action} ${fullName}: ${ex.message}"
                    totalFailures++
                }
            }
        }
    }

    directories.each { dir ->
        updateJobs(dir, "${namePrefix}${dir.name}/")
    }
}

def workspace = build.workspace
def xmlBase = new FilePath(workspace, 'XML_BASE_DIR_PLACEHOLDER')

def localDir = Files.createTempDirectory("TEMPORARY_XML_DIR_PREFIX_PLACEHOLDER")
println "Created local XML dir '${localDir}'"
def localXmlBase = new FilePath(localDir.toFile())

println 'Copy remote files to local XML dir'
xmlBase.copyRecursiveTo(localXmlBase)

try {
    println 'Start updating jobs from local XML dir'
    updateJobs(localXmlBase)
    println 'Updating jobs done'
} finally {
    println "Deleting local XML dir '${localDir}'"
    if (!localDir.toFile().deleteDir()) {
        println "Could not delete local XML dir '${localDir}'."
    }
}

println """\

    SEED JOB SUMMARY

    Created items : ${createdItems}
    Updated items : ${updatedItems}
    Total items   : ${totalItems}

    Created views : ${createdViews}
    Updated views : ${updatedViews}
    Total views   : ${totalViews}

    Total failures: ${totalFailures}
    """

if (totalFailures > 0) {
    throw new RuntimeException("Could not create/update ${totalFailures} elements")
}
