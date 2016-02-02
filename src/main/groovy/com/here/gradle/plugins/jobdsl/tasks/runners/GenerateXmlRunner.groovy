package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.util.DslConfig
import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.MemoryJobManagement

def properties = System.getProperties()

def configuration = new JsonSlurper().parseText(properties['configuration'])
DslConfig.setConfiguration(configuration)

def jobManagement = new MemoryJobManagement()

properties['inputFiles'].split(':').each { String filename ->
    println "Loading ${filename}"
    DslScriptLoader.runDslEngine(new File(filename).text, jobManagement)
}

def outputDirectory = new File(properties['outputDirectory'])
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
