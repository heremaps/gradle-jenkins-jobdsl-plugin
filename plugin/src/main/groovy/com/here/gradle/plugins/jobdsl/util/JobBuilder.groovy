package com.here.gradle.plugins.jobdsl.util

import com.here.gradle.plugins.jobdsl.GradleJobDslPluginException

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.jobs.BuildFlowJob
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import javaposse.jobdsl.dsl.jobs.MatrixJob
import javaposse.jobdsl.dsl.jobs.MavenJob
import javaposse.jobdsl.dsl.jobs.MultiJob
import javaposse.jobdsl.dsl.jobs.WorkflowJob

@Deprecated
class JobBuilder {

    DslFactory dslFactory
    String name

    Job build(Class<? extends Job> jobClass, @DelegatesTo(Job) Closure closure) {
        switch (jobClass) {
            case BuildFlowJob:
                return dslFactory.buildFlowJob(name, closure)
            case FreeStyleJob:
                return dslFactory.freeStyleJob(name, closure)
            case MatrixJob:
                return dslFactory.matrixJob(name, closure)
            case MavenJob:
                return dslFactory.mavenJob(name, closure)
            case MultiJob:
                return dslFactory.multiJob(name, closure)
            case WorkflowJob:
                return dslFactory.pipelineJob(name, closure)
            default:
                throw new GradleJobDslPluginException("Job type ${jobClass} is not supported.")
        }
    }

    BuildFlowJob buildBuildFlowJob(@DelegatesTo(BuildFlowJob) Closure closure) {
        return build(BuildFlowJob, closure)
    }

    FreeStyleJob buildFreeStyleJob(@DelegatesTo(FreeStyleJob) Closure closure) {
        return build(FreeStyleJob, closure)
    }

    MatrixJob buildMatrixJob(@DelegatesTo(MatrixJob) Closure closure) {
        return build(MatrixJob, closure)
    }

    MavenJob buildMavenJob(@DelegatesTo(MavenJob) Closure closure) {
        return build(MavenJob, closure)
    }

    MultiJob buildMultiJob(@DelegatesTo(MultiJob) Closure closure) {
        return build(MultiJob, closure)
    }

    WorkflowJob buildPipelineJob(@DelegatesTo(WorkflowJob) Closure closure) {
        return build(WorkflowJob, closure)
    }

}
