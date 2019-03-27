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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

/**
 * Gradle plugin configuration for the Job DSL plugin. Configures tasks, extensions, and source sets.
 */
class JobDslPlugin implements Plugin<Project> {

    @Override
    @SuppressWarnings('DuplicateMapLiteral')
    void apply(Project project) {
        project.extensions.create('jobdsl', JobDslPluginExtension, project)

        project.plugins.apply('groovy')

        project.sourceSets {
            jobdsl {
                groovy {
                    srcDirs 'src/jobdsl'
                }
            }
        }

        project.configurations {
            jenkinsPlugins {
                description 'Jenkins plugins that are required to process the Job DSL scripts.'
            }
        }

        project.repositories {
            mavenCentral()

            maven {
                url 'https://repo.jenkins-ci.org/releases/'
            }
        }

        project.dependencies {
            // This Jenkins plugin needs to be loaded by the Jenkins instance to discover Job DSL extensions and support
            // the auto-generated DSL.
            jenkinsPlugins('org.jenkins-ci.plugins:job-dsl:1.67') {
                exclude(module: 'groovy-all')
            }

            // These JAR dependencies are required by the Gradle Exec task which executes the DSL scripts.
            jenkinsPlugins('org.jenkins-ci.plugins:job-dsl:1.67@jar') {
                exclude(module: 'groovy-all')
            }
            jenkinsPlugins 'org.jenkins-ci.plugins:structs:1.9@jar'
        }

        project.task('resolveJenkinsPlugins', type: Copy) {
            from project.configurations.jenkinsPlugins
            into new File(project.sourceSets.main.output.resourcesDir, 'test-dependencies')
            include '*.hpi'
            include '*.jpi'

            doLast {
                def baseNames = source.collect { it.name.take(it.name.lastIndexOf('.')) }
                new File(destinationDir, 'index').setText(baseNames.join('\n'), 'UTF-8')
            }
        }

        def execTasks = []
        execTasks += project.task('dslGenerateXml', type: GenerateXmlTask) {
            buildDirectory(project.buildDir.absolutePath)
        }

        execTasks += project.task('dslUpdateJenkins', type: UpdateJenkinsTask) {
            buildDirectory(project.buildDir.absolutePath)
        }

        execTasks.each { task ->
            task.dependsOn 'classes', 'resolveJenkinsPlugins'
        }
    }

}
