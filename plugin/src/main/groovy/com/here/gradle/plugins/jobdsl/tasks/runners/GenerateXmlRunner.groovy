package com.here.gradle.plugins.jobdsl.tasks.runners

import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.MemoryJobManagement

class GenerateXmlRunner extends AbstractTaskRunner {

    public static void main(String[] args) {
        new GenerateXmlRunner().run()
    }

    @Override
    JobManagement createJobManagement() {
        new MemoryJobManagement()
    }

    @Override
    void postProcess() {
        def outputDirectory = new File(runProperties['outputDirectory'])
        outputDirectory.deleteDir()
        outputDirectory.mkdirs()
        jobManagement.savedConfigs.each { String name, String xml ->
            def jobDirectory = outputDirectory
            def jobName = name
            if (name.contains('/')) {
                def lastIndex = name.lastIndexOf('/')
                def subDirectory = name.substring(0, lastIndex)
                jobDirectory = new File("${outputDirectory}/${subDirectory}")
                jobDirectory.mkdirs()
                jobName = name.substring(lastIndex + 1)
            }
            def xmlFile = new File("${jobDirectory}/${jobName}.xml")
            xmlFile.write(xml)
        }

        jobManagement.savedViews.each { String name, String xml ->
            def xmlFile = new File("${outputDirectory}/${name}.xml")
            xmlFile.write(xml)
        }
    }
}
