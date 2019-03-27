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

package com.here.gradle.plugins.jobdsl

import com.here.gradle.plugins.jobdsl.tasks.GenerateXmlTask
import com.here.gradle.plugins.jobdsl.tasks.UpdateJenkinsTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Test the Gradle configuration of the plugin.
 */
class JobDslPluginTest extends Specification {

    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def 'resolveJenkinsPlugins task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.resolveJenkinsPlugins instanceof Copy
    }

    def 'dslGenerateXml task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.dslGenerateXml instanceof GenerateXmlTask
        project.tasks.dslUpdateJenkins.dependsOn.contains('resolveJenkinsPlugins')
    }

    def 'dslUpdateJenkins task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.dslUpdateJenkins instanceof UpdateJenkinsTask
        project.tasks.dslUpdateJenkins.dependsOn.contains('resolveJenkinsPlugins')
    }

    def 'groovy plugin is applied'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.plugins.hasPlugin('groovy')
    }

    def 'required repositories are added'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.repositories.find { it.url.toString() == 'https://repo.jenkins-ci.org/releases/' }
        project.repositories.find { it.url.toString() == 'https://repo.maven.apache.org/maven2/' }
    }

    def 'jobdsl source set is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.sourceSets.findByName('jobdsl')
    }

    def 'jenkinsPlugins configuration is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.configurations.findByName('jenkinsPlugins')
    }

    def 'jobdsl extension is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.extensions.findByName('jobdsl')
    }

}
