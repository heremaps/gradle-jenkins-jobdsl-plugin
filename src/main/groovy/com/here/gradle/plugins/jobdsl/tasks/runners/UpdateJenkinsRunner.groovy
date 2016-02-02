package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.RestJobManagement
import com.here.gradle.plugins.jobdsl.util.DslConfig
import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.DslScriptLoader

def properties = System.getProperties()

def jenkinsUrl = properties['jenkinsUrl']
def jenkinsUser = properties['jenkinsUser']
def jenkinsApiToken = properties['jenkinsApiToken']

def configuration = new JsonSlurper().parseText(properties['configuration'])
DslConfig.setConfiguration(configuration)

def jobManagement = new RestJobManagement(jenkinsUrl, jenkinsUser, jenkinsApiToken)

properties['inputFiles'].split(':').each { String filename ->
    println "Loading ${filename}"
    DslScriptLoader.runDslEngine(new File(filename).text, jobManagement)
}
