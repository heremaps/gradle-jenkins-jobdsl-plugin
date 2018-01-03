package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.GradleJobDslPluginException
import com.here.gradle.plugins.jobdsl.ItemFilter
import com.here.gradle.plugins.jobdsl.RestJobManagement

import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.JobManagement

import jenkins.model.Jenkins

/**
 * Performs the action of the {@link com.here.gradle.plugins.jobdsl.tasks.UpdateJenkinsTask} to upload all items and
 * views to a Jenkins instance using the REST API.
 */
class UpdateJenkinsRunner extends AbstractTaskRunner {

    static void main(String[] args) {
        new UpdateJenkinsRunner().run()
    }

    @Override
    JobManagement createJobManagement(Jenkins jenkins, ItemFilter filter) {
        boolean disablePluginChecks = runProperties['disablePluginChecks'].toBoolean()
        boolean dryRun = runProperties['dryRun'].toBoolean()
        String jenkinsUrl = runProperties['jenkinsUrl']
        String jenkinsUser = runProperties['jenkinsUser']
        String jenkinsApiToken = runProperties['jenkinsApiToken']

        new RestJobManagement(filter, disablePluginChecks, dryRun, jenkinsUrl, jenkinsUser, jenkinsApiToken, jenkins)
    }

    @Override
    void postProcess(Jenkins jenkins, List<GeneratedItems> generatedItems, ItemFilter filter) {
        println '\ndslUpdateJenkins results:'
        def restJobManagement = (RestJobManagement) jobManagement

        int longestKey = restJobManagement.statusCounter.keySet().inject(0) { maxLength, key ->
            key.length() > maxLength ? key.length() : maxLength
        }

        restJobManagement.statusCounter.keySet().sort(false).each { key ->
            println "${key.padRight(longestKey)}: ${restJobManagement.statusCounter[key]}"
        }

        if (restJobManagement.disablePluginChecks) {
            println 'Plugin compatibility checks are disabled.'
        } else {
            printPluginList(restJobManagement.deprecatedPlugins, 'Deprecated')
            printPluginList(restJobManagement.missingPlugins, 'Missing')
            printPluginList(restJobManagement.outdatedPlugins, 'Outdated')
            println ''
        }

        if (restJobManagement.statusCounter[RestJobManagement.STATUS_COULD_NOT_CREATE] > 0
                || restJobManagement.statusCounter[RestJobManagement.STATUS_COULD_NOT_UPDATE] > 0) {
            throw new GradleJobDslPluginException('Some items or view could not be updated. Check the log for details.')
        }
    }

    private void printPluginList(Set<String> plugins, String name) {
        if (!plugins.isEmpty()) {
            println "\n${name} plugins:"
            plugins.each { println "  ${it}" }
        }
    }

}
