package com.here.gradle.plugins.jobdsl

import com.here.gradle.plugins.jobdsl.tasks.GenerateXmlTask
import com.here.gradle.plugins.jobdsl.tasks.UpdateJenkinsTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class JobDslPluginTest extends Specification {

    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def 'dslGenerateXml task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.dslGenerateXml instanceof GenerateXmlTask
    }

    def 'dslUpdateJenkins task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.dslUpdateJenkins instanceof UpdateJenkinsTask
    }

    def 'groovy plugin is applied'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.plugins.hasPlugin('groovy')
    }

    def 'jobdsl source set is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.sourceSets.findByName('jobdsl')
    }

    def 'jobdsl extension is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.extensions.findByName('jobdsl')
    }

}
