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

    /**
     * Create the job object using the configured DSL closures and job class.
     *
     * @return The created {@link Job} object.
     * @throws RuntimeException if job type is null or not supported.
     */
    final Job build() {
        checkNameIsValid()
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

    /**
     * Set the job type to {@link BuildFlowJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void buildFlowJob(@DelegatesTo(BuildFlowJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = BuildFlowJob
    }

    /**
     * Set the job type to {@link FreeStyleJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void freeStyleJob(@DelegatesTo(FreeStyleJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = FreeStyleJob
    }

    /**
     * Set the job type to {@link MatrixJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void matrixJob(@DelegatesTo(MatrixJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = MatrixJob
    }

    /**
     * Set the job type to {@link MavenJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void mavenJob(@DelegatesTo(MavenJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = MavenJob
    }

    /**
     * Set the job type to {@link MultiJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void multiJob(@DelegatesTo(MultiJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = MultiJob
    }

    /**
     * Set the job type to {@link WorkflowJob} and add the provided DSL closure to the list of closures.
     *
     * @param closure
     */
    void workflowJob(@DelegatesTo(WorkflowJob) Closure closure) {
        checkJobClassNull()
        addDsl(closure)
        jobClass = WorkflowJob
    }

    /**
     * Add a DSL closure to the list of closures that will be used to create the job.
     *
     * @param closure
     */
    void addDsl(@DelegatesTo(Job) Closure closure) {
        if (closure == null) {
            throw new RuntimeException('Closure must not be null.')
        }
        dslClosures.add(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link FreeStyleJob}. The closure is
     * forwarded to {@link JobBuilder2#addDsl}.

     * @param closure
     */
    void addFreeStyleDsl(@DelegatesTo(FreeStyleJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link MatrixJob}. The closure is
     * forwarded to {@link JobBuilder2#addDsl}.

     * @param closure
     */
    void addMatrixDsl(@DelegatesTo(MatrixJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link MavenJob}. The closure is
     * forwarded to {@link JobBuilder2#addDsl}.

     * @param closure
     */
    void addMavenDsl(@DelegatesTo(MavenJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link MultiJob}. The closure is
     * forwarded to {@link JobBuilder2#addDsl}.

     * @param closure
     */
    void addMultiDsl(@DelegatesTo(MultiJob) Closure closure) {
        addDsl(closure)
    }

    /**
     * Convenience method to get full IDE support when adding a DSL closure for a @{link WorkflowJob}. The closure is
     * forwarded to {@link JobBuilder2#addDsl}.

     * @param closure
     */
    void addWorkflowDsl(@DelegatesTo(WorkflowJob) Closure closure) {
        addDsl(closure)
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

    /**
     * Return the full job name including folders.
     *
     * @return
     */
    String fullJobName() {
        return (folders + name).join('/')
    }

    void checkNameIsValid() {
        if (name.contains('/')) {
            throw new RuntimeException('Job name may not contain "/", if the job is inside a folder use the folders ' +
                    'field.')
        }
    }

}
