package com.here.gradle.plugins.jobdsl.tasks

import com.here.gradle.plugins.jobdsl.ServerDefinition
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.JavaExec

class UpdateJenkinsTask extends JavaExec {

    String jenkinsUrl
    String jenkinsUser
    String jenkinsApiToken
    String serverName
    ServerDefinition server

    UpdateJenkinsTask() {
        super()
        group = 'Job DSL'
        description = 'Update jobs on Jenkins.'
    }

    @Override
    void exec() {
        if (serverName != null) {
            server = project.jobdsl.servers.find { it.name == serverName }

            if (server == null) {
                throw new GradleException("Server '${serverName}' is not configured in the build script.")
            }

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

    @Option(option = 'server', description = 'Name of the Jenkins server configuration.')
    void setServerName(String serverName) {
        this.serverName = serverName
    }

}
