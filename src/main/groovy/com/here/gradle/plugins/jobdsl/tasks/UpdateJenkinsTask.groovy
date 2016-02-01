package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.JavaExec

class UpdateJenkinsTask extends JavaExec {

    private String jenkinsUrl
    private String jenkinsUser
    private String jenkinsApiToken

    UpdateJenkinsTask() {
        super()
        group = 'Job DSL'
        description = 'Update jobs on Jenkins.'
    }

    @Override
    void exec() {
        if (jenkinsUrl == null) {
            jenkinsUrl = project.dsl.jenkinsUrl
        }

        if (jenkinsUser == null) {
            jenkinsUser = project.jobdsl.jenkinsUser
        }

        if (jenkinsApiToken == null) {
            jenkinsApiToken = project.jobdsl.jenkinsApiToken
        }

        Map properties = [
                jenkinsUrl     : jenkinsUrl,
                jenkinsUser    : jenkinsUser,
                jenkinsApiToken: jenkinsApiToken,
                inputFiles     : project.sourceSets.jobdsl.allGroovy.asPath,
        ]
        setSystemProperties(properties)

        setMain('com.here.gradle.plugins.jobdsl.tasks.runners.UpdateJenkinsRunner')
        setClasspath(project.sourceSets.main.runtimeClasspath)

        super.exec()
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
