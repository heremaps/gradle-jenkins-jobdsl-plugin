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

package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.Job

/**
 * Class to manage a list of jobs that share some configuration. See the field descriptions for configuration options.
 * For usage details see the examples and the plugin documentation.
 */
class PipelineBuilder {

    /**
     * List of jobs that are built when calling {@link PipelineBuilder#build()}. The configuration of this builder is
     * applied to all jobs only when {@link PipelineBuilder#build()} is called.
     */
    List<PipelineJobBuilder> jobBuilders = []

    /**
     * These folders will be prepended to the folders configured in the jobs.
     */
    List<String> baseFolders

    /**
     * For each key/value pair from this map {@link PipelineBuilder} will look for a field in each job builder matching
     * the key and set it to the configured value. If no such field exists the value will be ignored.
     */
    Map<String, Object> defaultConfiguration = [:]

    /**
     * Closures added to this list will be added to the list of Job DSL closures of each job builder.
     */
    Closure commonDsl = { }

    void addJob(PipelineJobBuilder jobBuilder) {
        jobBuilder.pipelineBuilder = this
        jobBuilders.add(jobBuilder)
    }

    void defaultConfiguration(Map<String, Object> defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration
    }

    void commonDsl(@DelegatesTo(Job) Closure commonDsl) {
        this.commonDsl = commonDsl
    }

    void baseFolders(List<String> baseFolders) {
        this.baseFolders = [*baseFolders]
    }

    void build() {
        jobBuilders.each { jobBuilder ->
            applyDefaultConfiguration(jobBuilder)
            jobBuilder.build(commonDsl)
        }
    }

    final applyDefaultConfiguration(PipelineJobBuilder jobBuilder) {
        defaultConfiguration.each { key, value ->
            if (jobBuilder.hasProperty(key) && jobBuilder.getProperty(key) == null) {
                jobBuilder."${key}" = value
            } else {
                println "Ignoring default configuration '${key}' for job '${jobBuilder.fullJobName()}'"
            }
        }
    }

}
