package com.here.demo.jobdsl

import com.here.gradle.plugins.jobdsl.util.PipelineJobBuilder
import javaposse.jobdsl.dsl.DslFactory

/**
 * Demonstrates how to use {@link PipelineJobBuilder} to properties to simplify the configuration of job templates.
 */
class DemoPipelineJobBuilder extends PipelineJobBuilder {

    String shellCommand
    Integer timeout

    DemoPipelineJobBuilder(DslFactory dslFactory) {
        super(dslFactory)

        addDsl {
            steps {
                shell(shellCommand)
            }
            wrappers {
                timeout {
                    absolute(timeout)
                }
            }
        }
    }

}
