package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.api.internal.tasks.options.Option
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
        }

        return [
                disablePluginChecks: disablePluginChecks,
                dryRun             : dryRun,
                jenkinsUrl         : jenkinsUrl,
                jenkinsUser        : jenkinsUser,
                jenkinsApiToken    : jenkinsApiToken
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

}
