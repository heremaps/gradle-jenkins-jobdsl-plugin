package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.api.internal.tasks.options.Option

class UpdateJenkinsTask extends AbstractDslTask {

    String jenkinsUrl
    String jenkinsUser
    String jenkinsApiToken

    UpdateJenkinsTask() {
        super()
        description = 'Update jobs on Jenkins.'
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
                jenkinsUrl     : jenkinsUrl,
                jenkinsUser    : jenkinsUser,
                jenkinsApiToken: jenkinsApiToken
        ]
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
