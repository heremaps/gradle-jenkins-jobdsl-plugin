package com.here.demo.jobdsl

import com.here.gradle.plugins.jobdsl.util.PipelineJobBuilder
import javaposse.jobdsl.dsl.DslFactory

class DemoPipelineJobBuilder extends PipelineJobBuilder {

    String shellCommand
    int timeout = 30

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
