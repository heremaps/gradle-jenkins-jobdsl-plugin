package com.here.demo.jobdsl

import com.here.gradle.plugins.jobdsl.util.DslConfig
import com.here.gradle.plugins.jobdsl.util.JobBuilder
import javaposse.jobdsl.dsl.Job

/**
 * Example for a job builder that takes a configuration closure and adds some default behaviour. This is useful when
 * you want to create a template for the common behaviour of many different jobs.<br>
 * This example also uses {@link DslConfig} to retrieve the configuration of the project.
 */
class TemplateJobBuilder extends JobBuilder {

    @Override
    Job build(Class<? extends Job> jobClass, @DelegatesTo(Job) Closure closure) {
        def job = super.build(jobClass, closure)
        job.with {
            disabled(!DslConfig.get('enableJobs'))

            wrappers {
                timestamps()
            }
        }
        return job
    }

}
