package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.FilteringJenkinsJobManagement
import com.here.gradle.plugins.jobdsl.GradleJobDslPluginException
import com.here.gradle.plugins.jobdsl.ItemFilter

import hudson.model.AbstractItem
import hudson.model.Item
import hudson.model.ItemGroup

import java.nio.file.Files

import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.JobManagement

import jenkins.model.Jenkins

/**
 * Performs the action of the {@link com.here.gradle.plugins.jobdsl.tasks.GenerateXmlTask} to generate XML files for all
 * items and views.
 */
class GenerateXmlRunner extends AbstractTaskRunner {

    static void main(String[] args) {
        new GenerateXmlRunner().run()
    }

    @Override
    JobManagement createJobManagement(Jenkins jenkins, ItemFilter filter) {
        def workspace = Files.createTempDirectory('jobdsl').toFile()
        workspace.deleteOnExit()

        new FilteringJenkinsJobManagement(filter, System.out, [:], workspace)
    }

    @Override
    @SuppressWarnings('Instanceof')
    void postProcess(Jenkins jenkins, List<GeneratedItems> generatedItems, ItemFilter filter) {
        def outputDirectory = new File(runProperties['outputDirectory'])
        outputDirectory.deleteDir()
        outputDirectory.mkdirs()

        generatedItems.each {
            it.jobs.each { generatedJob ->
                if (filter.matches(generatedJob.jobName)) {
                    def item = jenkins.getItemByFullName(generatedJob.jobName)
                    if (item instanceof AbstractItem) {
                        writeXml(outputDirectory, item.fullName, item.configFile.asString())
                    } else {
                        throw new GradleJobDslPluginException(
                                "Unsupported item type for item '${generatedJob.jobName}': ${item.getClass().name}")
                    }
                }
            }
        }

        generatedItems.each {
            it.views.each { generatedView ->
                if (filter.matches(generatedView.name)) {
                    def parent = findParentItem(jenkins, generatedView.name)
                    def view = parent.getView(nameWithoutFolders(generatedView.name))
                    def viewName = parent == jenkins ? view.viewName : "${parent.fullName}/${view.viewName}"
                    def outputStream = new ByteArrayOutputStream()
                    view.writeXml(outputStream)
                    def xml = new String(outputStream.toByteArray(), 'UTF-8')
                    writeXml(outputDirectory, viewName, xml)
                }
            }
        }
    }

    def nameWithoutFolders(String name) {
        name.split('/').last()
    }

    @SuppressWarnings('Instanceof')
    def findParentItem(Jenkins jenkins, String name) {
        int index = name.lastIndexOf('/')
        switch (index) {
            case -1:
            case 0:
                return jenkins
            default:
                def parentName = name[0..index - 1]
                Item item = jenkins.getItemByFullName(parentName)
                return item instanceof ItemGroup ? (ItemGroup) item : null
        }
    }

    void writeXml(File outputDirectory, String name, String xml) {
        def targetDirectory = outputDirectory
        def fileName = name

        if (name.contains('/')) {
            def lastIndex = name.lastIndexOf('/')
            def subDirectory = name[0..lastIndex]
            targetDirectory = new File("${outputDirectory}/${subDirectory}")
            targetDirectory.mkdirs()
            fileName = name.drop(lastIndex + 1)
        }

        new File("${targetDirectory}/${fileName}.xml").withWriter('UTF-8') {
            it.write(xml)
        }
    }

}
