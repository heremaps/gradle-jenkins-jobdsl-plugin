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

import com.here.gradle.plugins.jobdsl.ServerDefinition
import com.here.gradle.plugins.jobdsl.util.PathComparator
import groovy.json.JsonBuilder
import org.gradle.api.GradleException
import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.util.CollectionUtils

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

        def groovyFiles = project.sourceSets.jobdsl.allGroovy.files*.absolutePath
        groovyFiles.sort(new PathComparator())

        def properties = getProperties()
        properties['buildDirectory'] = buildDirectoryPath
        properties['configuration'] = encodeBase64(new JsonBuilder(project.jobdsl.configuration).toString())
        properties['filter'] = encodeBase64(filter)
        properties['inputFiles'] = CollectionUtils.join(File.pathSeparator, groovyFiles)
        properties['serverConfiguration'] = server != null ?
                encodeBase64(new JsonBuilder(server.configuration).toString()) : ''
        properties['jna.nosys'] = true
        systemProperties = properties

        main = mainClass
        classpath = project.sourceSets.main.runtimeClasspath + project.buildscript.configurations.classpath +
                project.configurations.jenkinsPlugins
        jvmArgs = [
                // Uncomment the following two lines for debugging the exec tasks.
                //'-Xdebug',
                //'-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'
        ]

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
