package com.here.gradle.plugins.jobdsl.tasks

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.MemoryJobManagement
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GenerateXmlTask extends DefaultTask {

    GenerateXmlTask() {
        super()
        group = 'Job DSL'
        description = 'Generate XML for all jobs.'
    }

    @TaskAction
    void executeTask() {
        def jobManagement = new MemoryJobManagement()

        project.fileTree(dir: project.jobdsl.source, include: '*.groovy').each { File file ->
            println "Loading ${file.name}"
            DslScriptLoader.runDslEngine(file.text, jobManagement)
        }

        def outputDirectory = project.file("${project.buildDir}/jobdsl/xml")
        outputDirectory.deleteDir()
        outputDirectory.mkdirs()
        jobManagement.savedConfigs.each { String name, String xml ->
            def jobDirectory = outputDirectory
            def jobName = name
            if (name.contains('/')) {
                def lastIndex = name.lastIndexOf('/')
                def subDirectory = name.substring(0, lastIndex)
                jobDirectory = project.file("${outputDirectory}/${subDirectory}")
                jobDirectory.mkdirs()
                jobName = name.substring(lastIndex + 1)
            }
            def xmlFile = project.file("${jobDirectory}/${jobName}.xml")
            xmlFile.write(xml)
        }
    }

}
