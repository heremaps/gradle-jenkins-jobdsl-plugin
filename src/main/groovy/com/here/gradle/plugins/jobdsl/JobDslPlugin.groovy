package com.here.gradle.plugins.jobdsl

import org.gradle.api.Plugin
import org.gradle.api.Project

class JobDslPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('hello') << {
            println "Hello!"
        }
    }
}
