package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.api.internal.tasks.options.Option

class UpdateJenkinsTask extends AbstractDslTask {

    boolean dryRun
    String jenkinsUrl
    String jenkinsUser
    String jenkinsApiToken

    UpdateJenkinsTask() {
        super()
        description = 'Update jobs on Jenkins.'
        dryRun = false
    }

    @Override
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
                dryRun         : dryRun,
                jenkinsUrl     : jenkinsUrl,
                jenkinsUser    : jenkinsUser,
                jenkinsApiToken: jenkinsApiToken
        ]
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
