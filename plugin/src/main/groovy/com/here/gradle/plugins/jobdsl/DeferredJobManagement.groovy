package com.here.gradle.plugins.jobdsl

/**
 * Interface for an implementation of {@link javaposse.jobdsl.dsl.JobManagement} that does not apply the changes
 * immediately but stores them for later execution.
 */
interface DeferredJobManagement {

    void applyChanges()

}
