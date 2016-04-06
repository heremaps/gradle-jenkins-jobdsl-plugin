package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.ItemFilter
import com.here.gradle.plugins.jobdsl.RestJobManagement
import javaposse.jobdsl.dsl.JobManagement

class UpdateJenkinsRunner extends AbstractTaskRunner {

    public static void main(String[] args) {
        new UpdateJenkinsRunner().run()
    }

    @Override
    JobManagement createJobManagement(ItemFilter filter) {
        boolean dryRun = runProperties['dryRun'].toBoolean()
        String jenkinsUrl = runProperties['jenkinsUrl']
        String jenkinsUser = runProperties['jenkinsUser']
        String jenkinsApiToken = runProperties['jenkinsApiToken']

        new RestJobManagement(filter, dryRun, jenkinsUrl, jenkinsUser, jenkinsApiToken)
    }

    @Override
    void postProcess() {
        println '\ndslUpdateJenkins results:'
        def restJobManagement = (RestJobManagement) jobManagement

        int longestKey = restJobManagement.statusCounter.keySet().inject(0) { maxLength, key ->
            key.length() > maxLength ? key.length() : maxLength
        }

        restJobManagement.statusCounter.keySet().sort(false).each { key ->
            println "${key.padRight(longestKey)}: ${restJobManagement.statusCounter[key]}"
        }

        printPluginList(restJobManagement.deprecatedPlugins, 'Deprecated')
        printPluginList(restJobManagement.missingPlugins, 'Missing')
        printPluginList(restJobManagement.outdatedPlugins, 'Outdated')
    }

    private void printPluginList(Set<String> plugins, String name) {
        if (!plugins.isEmpty()) {
            println "\n${name} plugins:"
            plugins.each { println "  ${it}" }
        }
    }

}
