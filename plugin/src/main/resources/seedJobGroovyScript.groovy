import hudson.FilePath
import hudson.model.Item
import hudson.model.ItemGroup
import hudson.model.ModifiableViewGroup
import hudson.model.View
import jenkins.model.Jenkins
import java.io.BufferedReader
import java.io.InputStreamReader
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

def localFile = new File("TEMPORARY_XML_DIR_PREFIX_PLACEHOLDER-${build.getNumber()}")
println "Create local XML dir '${localFile.absolutePath}'"
if (localFile.exists()) {
    throw new RuntimeException("Local XML dir '${localFile.absolutePath}' already exists")
}
if (!localFile.mkdir()) {
    throw new RuntimeException("Could not create local XML dir '${localFile.absolutePath}'")
}
def localXmlBase = new FilePath(localFile)

println 'Copy remote files to local XML dir'
xmlBase.copyRecursiveTo(localXmlBase)

println 'Start updating jobs from local XML dir'
updateJobs(localXmlBase)

println "Delete local XML dir '${localFile.absolutePath}'"
if (!localFile.deleteDir()) {
    throw new RuntimeException("Could not delete local XML dir '${localFile.absolutePath}'")
}

println """\
    SEED JOB SUMMARY

    Created items: ${createdItems}
    Updated items: ${updatedItems}
    Total items  : ${totalItems}

    Created views: ${createdViews}
    Updated views: ${updatedViews}
    Total views  : ${totalViews}""".stripIndent()
