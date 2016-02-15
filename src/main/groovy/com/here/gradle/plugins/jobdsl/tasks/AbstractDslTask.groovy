package com.here.gradle.plugins.jobdsl.tasks

import com.here.gradle.plugins.jobdsl.ServerDefinition
import groovy.json.JsonBuilder
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.JavaExec

abstract class AbstractDslTask extends JavaExec {

    protected ServerDefinition server
    protected String serverName

    AbstractDslTask() {
        super()
        group = 'Job DSL'
    }

    @Override
    void exec() {
        // Pull serverName to local variable, otherwise Gradle throws this exception for an unknown reason:
        // groovy.lang.GroovyRuntimeException: Cannot get the value of write-only property 'serverName'
        def serverName = this.serverName

        if (serverName != null) {
            server = project.jobdsl.servers.find { it.name == serverName }

            if (server == null) {
                throw new GradleException("Server '${serverName}' is not configured in the build script.")
            } else {
                logger.quiet("Using server configuration '${server.name}'")
            }
        }

        def properties = getProperties()
        properties['configuration'] = new JsonBuilder(project.jobdsl.configuration).toString()
        properties['inputFiles'] = project.sourceSets.jobdsl.allGroovy.asPath
        properties['serverConfiguration'] = server != null ? new JsonBuilder(server.configuration).toString() : '{}'
        setSystemProperties(properties)

        setMain(getMainClass())
        setClasspath(project.sourceSets.main.runtimeClasspath)

        super.exec()
    }

    abstract String getMainClass()

    abstract Map<String, ?> getProperties()

    @Option(option = 'server', description = 'Name of the Jenkins server configuration.')
    void setServerName(String serverName) {
        this.serverName = serverName
    }

}
