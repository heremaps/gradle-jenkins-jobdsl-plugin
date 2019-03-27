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

import com.here.gradle.plugins.jobdsl.GradleJobDslPluginException

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import javaposse.jobdsl.dsl.jobs.MatrixJob
import javaposse.jobdsl.dsl.jobs.MavenJob
import javaposse.jobdsl.dsl.jobs.MultiJob
import javaposse.jobdsl.dsl.jobs.WorkflowJob

/**
 * This is the base class for all job builders. It can be extended to create templates for job configurations. It can
 * store multiple DSL closures which are concatenated when {@link JobBuilder#build()} is called. They are executed in
 * the order they are added to the builder. For detailed usage see the README file and the examples.
 */
class JobBuilder {

    DslFactory dslFactory
    Class<? extends Job> jobClass
    String name
    List<String> folders = []
    List<Closure> dslClosures = []

    JobBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    /**
     * Create the job object using the configured DSL closures and job class.
     *
     * @param dslClosure An optional Job DSL Closure that will be applied to the job builder.
     *
     * @return The created {@link Job} object.
     */
    final Job build(Closure dslClosure = { }) {
        checkNameIsValid()
        def combinedClosure = concatenateDslClosures(dslClosure)
        switch (jobClass) {
            case FreeStyleJob:
                return dslFactory.freeStyleJob(fullJobName(), combinedClosure)
            case MatrixJob:
                return dslFactory.matrixJob(fullJobName(), combinedClosure)
            case MavenJob:
                return dslFactory.mavenJob(fullJobName(), combinedClosure)
            case MultiJob:
                return dslFactory.multiJob(fullJobName(), combinedClosure)
            case WorkflowJob:
                return dslFactory.pipelineJob(fullJobName(), combinedClosure)
            default:
                throw new GradleJobDslPluginException("Job type ${jobClass} is not supported.")
        }
    }

    /**
     * Set the job type to {@link FreeStyleJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void freeStyleJob(@DelegatesTo(FreeStyleJob) Closure closure = null) {
        checkJobClassNull()
        if (closure != null) {
            addDsl(closure)
        }
        jobClass = FreeStyleJob
    }

    /**
     * Set the job type to {@link MatrixJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void matrixJob(@DelegatesTo(MatrixJob) Closure closure = null) {
        checkJobClassNull()
        if (closure != null) {
            addDsl(closure)
        }
        jobClass = MatrixJob
    }

    /**
     * Set the job type to {@link MavenJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void mavenJob(@DelegatesTo(MavenJob) Closure closure = null) {
        checkJobClassNull()
        if (closure != null) {
            addDsl(closure)
        }
        jobClass = MavenJob
    }

    /**
     * Set the job type to {@link MultiJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void multiJob(@DelegatesTo(MultiJob) Closure closure = null) {
        checkJobClassNull()
        if (closure != null) {
            addDsl(closure)
        }
        jobClass = MultiJob
    }

    /**
     * Set the job type to {@link WorkflowJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void pipelineJob(@DelegatesTo(WorkflowJob) Closure closure = null) {
        checkJobClassNull()
        if (closure != null) {
            addDsl(closure)
        }
        jobClass = WorkflowJob
    }

    /**
     * Add a DSL closure to the list of closures that will be used to create the job.
     *
     * @param closure
     */
    void addDsl(@DelegatesTo(Job) Closure closure) {
        if (closure == null) {
            throw new GradleJobDslPluginException('Closure must not be null.')
        }
        dslClosures.add(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link FreeStyleJob}. The closure is
     * forwarded to {@link JobBuilder#addDsl}.

     * @param closure
     */
    void addFreeStyleDsl(@DelegatesTo(FreeStyleJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link MatrixJob}. The closure is
     * forwarded to {@link JobBuilder#addDsl}.

     * @param closure
     */
    void addMatrixDsl(@DelegatesTo(MatrixJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link MavenJob}. The closure is
     * forwarded to {@link JobBuilder#addDsl}.

     * @param closure
     */
    void addMavenDsl(@DelegatesTo(MavenJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link MultiJob}. The closure is
     * forwarded to {@link JobBuilder#addDsl}.

     * @param closure
     */
    void addMultiDsl(@DelegatesTo(MultiJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link WorkflowJob}. The closure is
     * forwarded to {@link JobBuilder#addDsl}.

     * @param closure
     */
    void addPipelineDsl(@DelegatesTo(WorkflowJob) Closure closure) {
        addDsl(closure)
    }

    void checkJobClassNull() {
        if (jobClass != null) {
            throw new GradleJobDslPluginException('The job methods cannot be called multiple times, job class is ' +
                    "already set to ${jobClass.name}.")
        }
    }

    /**
     * Concatenate all {@link #dslClosures} to a single Closure.
     *
     * @param dslClosure An optional DSL Closure that will be appended to the result.
     *
     * @return The concatenated closure.
     */
    Closure concatenateDslClosures(Closure dslClosure = { }) {
        def allClosures = [*dslClosures, dslClosure]
        return allClosures.inject({ }) { acc, val -> acc >> val }
    }

    /**
     * Return the full job name including folders.
     */
    String fullJobName() {
        return (folders + name).join('/')
    }

    void checkNameIsValid() {
        if (name.contains('/')) {
            throw new GradleJobDslPluginException('Job name may not contain "/", if the job is inside a folder use ' +
                    'the folders field.')
        }
    }

}
