package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.util.DslConfig
import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.JobManagement

abstract class AbstractTaskRunner {

    protected JobManagement jobManagement
    protected Properties runProperties

    void run() {
        runProperties = System.getProperties()

        def slurper = new JsonSlurper()

        def configuration = slurper.parseText(runProperties['configuration'])
        DslConfig.setConfiguration(configuration)

        def serverConfiguration = slurper.parseText(runProperties['serverConfiguration'])
        DslConfig.setServerConfiguration(serverConfiguration)

        jobManagement = createJobManagement()

        runProperties['inputFiles'].split(':').each { String filename ->
            println "Loading ${filename}"
            DslScriptLoader.runDslEngine(new File(filename).text, jobManagement)
        }

        postProcess()
    }

    abstract JobManagement createJobManagement()

    void postProcess() {
        // do nothing by default
    }

}
