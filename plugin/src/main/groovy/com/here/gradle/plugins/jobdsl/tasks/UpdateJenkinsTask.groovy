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

package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Task that calls {@link com.here.gradle.plugins.jobdsl.tasks.runners.UpdateJenkinsRunner} to upload all items and
 * views configured in the project to a Jenkins instance using the REST API.
 */
class UpdateJenkinsTask extends AbstractDslTask {

    @Input
    @Optional
    boolean disablePluginChecks

    @Input
    @Optional
    boolean dryRun

    @Input
    @Optional
    String jenkinsUrl

    @Input
    @Optional
    String proxyUrl

    @Input
    @Optional
    String jenkinsUser

    @Input
    @Optional
    String jenkinsApiToken

    UpdateJenkinsTask() {
        super()
        description = 'Update jobs on Jenkins.'
        disablePluginChecks = false
        dryRun = false
    }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty') // Implements abstract method
    String getMainClass() {
        'com.here.gradle.plugins.jobdsl.tasks.runners.UpdateJenkinsRunner'
    }

    @Override
    Map<String, ?> getProperties() {
        if (server != null) {
            if (jenkinsUrl == null) {
                jenkinsUrl = server.jenkinsUrl
            }

            if (jenkinsUser == null) {
                jenkinsUser = server.jenkinsUser
            }

            if (jenkinsApiToken == null) {
                jenkinsApiToken = server.jenkinsApiToken
            }

            if (proxyUrl == null) {
                proxyUrl = server.proxyUrl
            }
        }

        return [
                disablePluginChecks: disablePluginChecks,
                dryRun             : dryRun,
                jenkinsUrl         : jenkinsUrl,
                jenkinsUser        : jenkinsUser,
                jenkinsApiToken    : jenkinsApiToken,
                proxyUrl           : proxyUrl
        ]
    }

    @Option(option = 'disablePluginChecks', description = 'Do not check compatibility of installed Jenkins plugins.')
    void setDisablePluginChecks(boolean disablePluginChecks) {
        this.disablePluginChecks = disablePluginChecks
    }

    @Option(option = 'dryRun', description = 'Do not upload jobs to Jenkins.')
    void setDryRun(boolean dryRun) {
        this.dryRun = dryRun
    }

    @Option(option = 'jenkinsUrl', description = 'URL of the Jenkins server to update.')
    void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl
    }

    @Option(option = 'jenkinsUser', description = 'Jenkins username.')
    void setJenkinsUser(String jenkinsUser) {
        this.jenkinsUser = jenkinsUser
    }

    @Option(option = 'jenkinsApiToken', description = 'Jenkins API token.')
    void setJenkinsApiToken(String jenkinsApiToken) {
        this.jenkinsApiToken = jenkinsApiToken
    }

    @Option(option = 'proxyUrl', description = 'URL of the HTTP proxy used to communicate with Jenkins.')
    void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl
    }

}
