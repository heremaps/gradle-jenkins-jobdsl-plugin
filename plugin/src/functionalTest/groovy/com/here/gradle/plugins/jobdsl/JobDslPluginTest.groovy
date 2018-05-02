package com.here.gradle.plugins.jobdsl

import com.here.gradle.plugins.jobdsl.tasks.GenerateXmlTask
import com.here.gradle.plugins.jobdsl.tasks.UpdateJenkinsTask
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Test the Gradle configuration of the plugin.
 */
class JobDslPluginTest extends Specification {

    private Project project

    def setup() {
        project = ProjectBuilder.builder().build()
    }

    def 'resolveJenkinsPlugins task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.resolveJenkinsPlugins instanceof Copy
    }

    def 'dslGenerateXml task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.dslGenerateXml instanceof GenerateXmlTask
        project.tasks.dslUpdateJenkins.dependsOn.contains('resolveJenkinsPlugins')
    }

    def 'dslUpdateJenkins task is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.tasks.dslUpdateJenkins instanceof UpdateJenkinsTask
        project.tasks.dslUpdateJenkins.dependsOn.contains('resolveJenkinsPlugins')
    }

    def 'groovy plugin is applied'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.plugins.hasPlugin('groovy')
    }

    def 'required repositories are added'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.repositories.find { it.url.toString() == 'https://repo.jenkins-ci.org/releases/' }
        project.repositories.find { it.url.toString() == 'https://repo.maven.apache.org/maven2/' }
    }

    def 'jobdsl source set is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.sourceSets.findByName('jobdsl')
    }

    def 'jenkinsPlugins configuration is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.configurations.findByName('jenkinsPlugins')
    }

    def 'jobdsl extension is created'() {
        when:
        project.pluginManager.apply('com.here.jobdsl')

        then:
        project.extensions.findByName('jobdsl')
    }

}
