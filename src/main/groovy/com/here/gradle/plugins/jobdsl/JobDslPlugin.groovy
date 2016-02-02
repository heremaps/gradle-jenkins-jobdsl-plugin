package com.here.gradle.plugins.jobdsl

import com.here.gradle.plugins.jobdsl.tasks.GenerateXmlTask
import com.here.gradle.plugins.jobdsl.tasks.UpdateJenkinsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class JobDslPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('jobdsl', JobDslPluginExtension, project)

        def execTasks = []
        execTasks += project.task('dslGenerateXml', type: GenerateXmlTask)
        execTasks += project.task('dslUpdateJenkins', type: UpdateJenkinsTask)

        project.plugins.apply('groovy')

        execTasks.each { task ->
            task.dependsOn 'classes'
        }

        project.sourceSets {
            jobdsl {
                groovy {
                    srcDirs 'src/jobdsl'
                }
            }
        }
    }

}
