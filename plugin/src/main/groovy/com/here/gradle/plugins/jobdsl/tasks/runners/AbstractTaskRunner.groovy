package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.ItemFilter
import com.here.gradle.plugins.jobdsl.util.DslConfig
import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.ScriptRequest

abstract class AbstractTaskRunner {

    protected JobManagement jobManagement
    protected Properties runProperties

    void run() {
        runProperties = System.getProperties()

        def slurper = new JsonSlurper()

        def configuration = slurper.parseText(decodeBase64(runProperties['configuration'].toString()))
        DslConfig.setConfiguration(configuration)

        def serverConfiguration = runProperties['serverConfiguration'].toString().length() == 0 ?
                [:] : slurper.parseText(decodeBase64(runProperties['serverConfiguration'].toString()))
        DslConfig.setServerConfiguration(serverConfiguration)

        def filter = new ItemFilter(decodeBase64(runProperties['filter']))
        jobManagement = createJobManagement(filter)
        DslScriptLoader loader = new DslScriptLoader(jobManagement)

        runProperties['inputFiles'].split(File.pathSeparator).each { String filename ->
            println "Loading ${filename}"
            ScriptRequest sriptRequest = new ScriptRequest(new File(filename).text)
            loader.runScripts([scriptRequest])
        }

        postProcess()
    }

    abstract JobManagement createJobManagement(ItemFilter filter)

    void postProcess() {
        // do nothing by default
    }

    private String decodeBase64(String encoded) {
        new String(encoded.decodeBase64())
    }

}
