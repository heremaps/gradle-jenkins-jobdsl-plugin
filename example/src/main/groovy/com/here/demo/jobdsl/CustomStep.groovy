package com.here.demo.jobdsl

import javaposse.jobdsl.dsl.helpers.step.StepContext

/**
 * Example for a class that implements a custom method to use in DSL scripts. This is useful when you want to re-use the
 * same build step in many different jobs.<br>
 * In this example the {@link StepContext} is used because the method will be used in the step closure of the DSL
 * script. If you want to add a custom method for another part of Job DSL you will have to use another context class.
 */
class CustomStep {

    static void echo(StepContext context, String output) {
        context.with {
            shell("echo ${output}")
        }
    }

}
