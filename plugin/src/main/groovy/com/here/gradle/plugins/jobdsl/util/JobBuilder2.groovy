package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.BuildFlowJob
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import javaposse.jobdsl.dsl.jobs.MatrixJob
import javaposse.jobdsl.dsl.jobs.MavenJob
import javaposse.jobdsl.dsl.jobs.MultiJob
import javaposse.jobdsl.dsl.jobs.WorkflowJob


/**
 * This is the new implementation of {@link JobBuilder}, because the API has changed the old version will be kept until
 * the next major release. This class will be renamed to JobBuilder with the next major release.
 * The main difference to the old implementation is that the DSL script can be provided before calling the build method
 * and the build method does not take any parameters anymore. This makes it possible to reuse the same builder instance
 * and build the job again after changing the configuration of the builder.
 */
class JobBuilder2 {

    DslFactory dslFactory
    Class<? extends Job> jobClass
    String name
    List<String> folders = []
    List<Closure> dslClosures = []

    JobBuilder2(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    final Job build() {
        def dslClosure = concatenateDslClosures()
        switch (jobClass) {
            case BuildFlowJob:
                return dslFactory.buildFlowJob(fullJobName(), dslClosure)
            case FreeStyleJob:
                return dslFactory.freeStyleJob(fullJobName(), dslClosure)
            case MatrixJob:
                return dslFactory.matrixJob(fullJobName(), dslClosure)
            case MavenJob:
                return dslFactory.mavenJob(fullJobName(), dslClosure)
            case MultiJob:
                return dslFactory.multiJob(fullJobName(), dslClosure)
            case WorkflowJob:
                return dslFactory.workflowJob(fullJobName(), dslClosure)
            default:
                throw new RuntimeException("Job type ${jobClass} is not supported.")
        }
    }

    void buildFlowJob(@DelegatesTo(BuildFlowJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = BuildFlowJob
    }

    void freeStyleJob(@DelegatesTo(FreeStyleJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = FreeStyleJob
    }

    void matrixJob(@DelegatesTo(MatrixJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = MatrixJob
    }

    void mavenJob(@DelegatesTo(MavenJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = MavenJob
    }

    void multiJob(@DelegatesTo(MultiJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = MultiJob
    }

    void workflowJob(@DelegatesTo(WorkflowJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = WorkflowJob
    }

    void addDsl(@DelegatesTo(Job) Closure closure) {
        if (closure == null) {
            throw new RuntimeException('Closure must not be null.')
        }
        dslClosures.add(closure)
    }

    void checkJobClassNull() {
        if (jobClass != null) {
            throw new RuntimeException('The job methods cannot be called multiple times, job class is already set to ' +
                    "${jobClass.name}.")
        }
    }

    Closure concatenateDslClosures() {
        return dslClosures.inject({}) { acc, val -> acc << val }
    }

    String fullJobName() {
        return (folders + name).join('/')
    }

}
