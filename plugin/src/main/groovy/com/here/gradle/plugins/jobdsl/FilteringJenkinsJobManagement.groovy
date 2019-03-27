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

import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.plugin.JenkinsJobManagement

/**
 * Extends {@link JenkinsJobManagement} to add support for an {@link ItemFilter}.
 */
class FilteringJenkinsJobManagement extends JenkinsJobManagement {

    private final ItemFilter filter

    FilteringJenkinsJobManagement(ItemFilter filter, PrintStream outputLogger, Map<String, ?> envVars,
                                  File workspace, boolean failOnMissingPlugin) {
        super(outputLogger, envVars, workspace)
        this.filter = filter
        this.failOnMissingPlugin = failOnMissingPlugin
    }

    @Override
    boolean createOrUpdateConfig(Item dslItem, boolean ignoreExisting) throws NameNotProvidedException {
        filter.matches(dslItem.name) ? super.createOrUpdateConfig(dslItem, ignoreExisting) : true
    }

    @Override
    void createOrUpdateView(String path, String config, boolean ignoreExisting) {
        if (filter.matches(path)) {
            super.createOrUpdateView(path, config, ignoreExisting)
        }
    }

}
