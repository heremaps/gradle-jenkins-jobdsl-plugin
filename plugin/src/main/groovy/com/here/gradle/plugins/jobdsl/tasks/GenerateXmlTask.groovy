package com.here.gradle.plugins.jobdsl.tasks

class GenerateXmlTask extends AbstractDslTask {

    GenerateXmlTask() {
        super()
        description = 'Generate XML for all jobs.'
    }

    @Override
    @SuppressWarnings('GetterMethodCouldBeProperty') // Implements abstract method
    String getMainClass() {
        'com.here.gradle.plugins.jobdsl.tasks.runners.GenerateXmlRunner'
    }

    @Override
    Map<String, ?> getProperties() {
        [
                outputDirectory: "${project.buildDir}/jobdsl/xml"
        ]
    }

}
