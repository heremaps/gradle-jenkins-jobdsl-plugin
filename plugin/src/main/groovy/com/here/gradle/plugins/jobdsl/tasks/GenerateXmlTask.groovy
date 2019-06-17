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

import org.gradle.api.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Task that calls {@link com.here.gradle.plugins.jobdsl.tasks.runners.GenerateXmlRunner} to generate XML files for
 * all items and views configured in the project.
 */
class GenerateXmlTask extends AbstractDslTask {

    @Input
    @Optional
    boolean failOnMissingPlugin

    GenerateXmlTask() {
        super()
        description = 'Generate XML for all jobs.'
        failOnMissingPlugin = false
    }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty') // Implements abstract method
    String getMainClass() {
        'com.here.gradle.plugins.jobdsl.tasks.runners.GenerateXmlRunner'
    }

    @Override
    Map<String, ?> getProperties() {
        [
                outputDirectory    : "${project.buildDir}/jobdsl/xml",
                failOnMissingPlugin: failOnMissingPlugin,
        ]
    }

    @Option(option = 'failOnMissingPlugin', description = 'Fail the task if a required plugin is missing.')
    void setFailOnMissingPlugin(boolean failOnMissingPlugin) {
        this.failOnMissingPlugin = failOnMissingPlugin
    }

}
