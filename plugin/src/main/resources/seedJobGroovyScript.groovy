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

def updateItem(Item item, String name, FilePath file) {
    println "  Update item ${name}"

    try {
        String oldConfig = item.getConfigFile().asString()
        String newConfig = file.read().getText('UTF-8')
        Diff diff = XMLUnit.compareXML(oldConfig, newConfig)
        if (diff.identical()) {
            println "  Item ${name} did not change"
            return
        }
    } catch (Exception e) {
        println "  WARNING: Could not create XML diff for ${name}"
    }

    def source = new StreamSource(file.read())
    item.updateByXml(source)
    updatedItems++
}

def createItem(ItemGroup parent, String name, FilePath file) {
    println "  Create item ${name}"
    if (parent instanceof ModifiableViewGroup) {
        parent.createProjectFromXML(name, file.read())
        createdItems++
    } else {
        println "  Could not create project in parent of type ${parent.class}"
    }
}

def updateView(View view, String name, FilePath file) {
    println "  Update view ${name}"
    def source = new StreamSource(file.read())
    view.updateByXml(source)
    updatedViews++
}

def createView(ItemGroup parent, String name, FilePath file) {
    println "  Create view ${name}"
    if (parent instanceof ModifiableViewGroup) {
        ((ModifiableViewGroup) parent).addView(View.createViewFromXML(name, file.read()))
        createdViews++
    } else {
        println "  Could not create view in parent of type ${parent.class}"
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
            return item instanceof ItemGroup ? (ItemGroup) item : null
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
            def reader = new BufferedReader(new InputStreamReader(file.read()))
            def parent = findParentItem(fullName)
            def firstLine = reader.readLine()
            if (firstLine.contains('View')) {
                totalViews++
                def view = parent.getView(name)
                if (view) {
                    updateView(view, name, file)
                } else {
                    createView(parent, name, file)
                }
            } else {
                totalItems++
                def item = Jenkins.getInstance().getItemByFullName(fullName)
                if (item) {
                    updateItem(item, name, file)
                } else {
                    createItem(parent, name, file)
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

    Created items: ${createdItems}
    Updated items: ${updatedItems}
    Total items  : ${totalItems}

    Created views: ${createdViews}
    Updated views: ${updatedViews}
    Total views  : ${totalViews}""".stripIndent()
