package com.here.gradle.plugins.jobdsl.tasks

import com.here.gradle.plugins.jobdsl.RestJobManagement
import javaposse.jobdsl.dsl.DslScriptLoader
import org.gradle.api.DefaultTask
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.TaskAction

class UpdateJenkinsTask extends DefaultTask {

    private String jenkinsUrl
    private String jenkinsUser
    private String jenkinsPassword

    UpdateJenkinsTask() {
        super()
        group = 'Job DSL'
        description = 'Update jobs on Jenkins.'
    }

    @TaskAction
    void executeTask() {
        def jobManagement = new RestJobManagement(jenkinsUrl, jenkinsUser, jenkinsPassword)

        project.fileTree(dir: project.jobdsl.source, include: '*.groovy').each { File file ->
            println "Loading ${file.name}"
            DslScriptLoader.runDslEngine(file.text, jobManagement)
        }

    }

    @Option(option = 'jenkinsUrl', description = 'URL of the Jenkins server to update.')
    void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl
    }

    @Option(option = 'jenkinsUser', description = 'Jenkins username.')
    void setJenkinsUser(String jenkinsUser) {
        this.jenkinsUser = jenkinsUser
    }

    @Option(option = 'jenkinsPassword', description = 'Jenkins password.')
    void setJenkinsPassword(String jenkinsPassword) {
        this.jenkinsPassword = jenkinsPassword
    }

}
