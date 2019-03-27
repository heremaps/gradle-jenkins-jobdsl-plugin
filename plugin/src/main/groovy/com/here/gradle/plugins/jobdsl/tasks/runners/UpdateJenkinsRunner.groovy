/*
 * Copyright (C) 2016-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

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
