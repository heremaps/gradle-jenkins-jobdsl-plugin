package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.api.internal.tasks.options.Option
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

/**
 * Task that calls {@link com.here.gradle.plugins.jobdsl.tasks.runners.GenerateXmlRunner} to generate XML files for
 * all items and views configured in the project.
 */
class GenerateXmlTask extends AbstractDslTask {

    @Input
    @Optional
    boolean failOnMissingPlugin

    GenerateXmlTask() {
        super()
        description = 'Generate XML for all jobs.'
        failOnMissingPlugin = false
    }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty') // Implements abstract method
    String getMainClass() {
        'com.here.gradle.plugins.jobdsl.tasks.runners.GenerateXmlRunner'
    }

    @Override
    Map<String, ?> getProperties() {
        [
                outputDirectory    : "${project.buildDir}/jobdsl/xml",
                failOnMissingPlugin: failOnMissingPlugin,
        ]
    }

    @Option(option = 'failOnMissingPlugin', description = 'Fail the task if a required plugin is missing.')
    void setFailOnMissingPlugin(boolean failOnMissingPlugin) {
        this.failOnMissingPlugin = failOnMissingPlugin
    }

}
