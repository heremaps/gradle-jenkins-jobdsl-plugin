package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.FilteringMemoryJobManagement
import com.here.gradle.plugins.jobdsl.ItemFilter
import javaposse.jobdsl.dsl.JobManagement

class GenerateXmlRunner extends AbstractTaskRunner {

    static void main(String[] args) {
        new GenerateXmlRunner().run()
    }

    @Override
    JobManagement createJobManagement(ItemFilter filter) {
        new FilteringMemoryJobManagement(filter)
    }

    @Override
    void postProcess() {
        def outputDirectory = new File(runProperties['outputDirectory'])
        outputDirectory.deleteDir()
        outputDirectory.mkdirs()
        jobManagement.savedConfigs.each { String name, String xml ->
            writeXml(outputDirectory, name, xml)
        }

        jobManagement.savedViews.each { String name, String xml ->
            writeXml(outputDirectory, name, xml)
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
        def xmlFile = new File("${targetDirectory}/${fileName}.xml")
        xmlFile.write(xml)
    }

}
