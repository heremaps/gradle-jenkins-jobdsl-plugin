package com.here.gradle.plugins.jobdsl.tasks

import com.here.gradle.plugins.jobdsl.ServerDefinition
import groovy.json.JsonBuilder
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec

/**
 * Shared code for all tasks of the plugin that call an implementation of
 * {@link com.here.gradle.plugins.jobdsl.tasks.runners.AbstractTaskRunner} to perform the action in another process.
 * This class takes care of forwarding all configuration options and the classpath to the external process.
 */
abstract class AbstractDslTask extends JavaExec {

    private String buildDirectoryPath = ''
    protected String filter = ''
    protected ServerDefinition server
    protected String serverName

    protected AbstractDslTask() {
        super()
        group = 'Job DSL'
    }

    AbstractDslTask buildDirectory(String buildDirectoryPath) {
        this.buildDirectoryPath = buildDirectoryPath
        return this
    }

    @Override
    @SuppressWarnings('UnnecessaryGetter') // getProperties() is much more readable than just properties in this case.
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
        properties['buildDirectory'] = buildDirectoryPath
        properties['configuration'] = encodeBase64(new JsonBuilder(project.jobdsl.configuration).toString())
        properties['filter'] = encodeBase64(filter)
        properties['inputFiles'] = project.sourceSets.jobdsl.allGroovy.asPath
        properties['serverConfiguration'] = server != null ?
                encodeBase64(new JsonBuilder(server.configuration).toString()) : ''
        systemProperties = properties

        main = mainClass
        classpath = project.sourceSets.main.runtimeClasspath + project.buildscript.configurations.classpath +
                project.configurations.jenkinsPlugins

        super.exec()
    }

    @Internal
    abstract String getMainClass()

    @Internal
    abstract Map<String, ?> getProperties()

    @Option(option = 'filter', description = 'Only evaluate item names that match this regular expression.')
    void setFilter(String filter) {
        this.filter = filter
    }

    @Option(option = 'server', description = 'Name of the Jenkins server configuration.')
    void setServerName(String serverName) {
        this.serverName = serverName
    }

    private String encodeBase64(String string) {
        string.bytes.encodeBase64().toString()
    }

}
