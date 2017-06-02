package com.here.demo.jobdsl

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

/**
 * Example for a job builder that takes only parameters but no configuration closure. This is useful when you have
 * many jobs which are almost identical.
 */
class EchoJobBuilder {

    DslFactory dslFactory
    String name
    String echo

    Job build() {
        def job = dslFactory.freeStyleJob(name) {
            steps {
                shell("echo ${echo}")
            }
        }
        return job
    }

}
