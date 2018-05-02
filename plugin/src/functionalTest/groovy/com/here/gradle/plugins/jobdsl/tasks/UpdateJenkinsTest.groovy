package com.here.gradle.plugins.jobdsl.tasks

import com.cloudbees.hudson.plugins.folder.Folder
import hudson.model.FreeStyleProject
import hudson.model.Item
import hudson.model.ListView
import hudson.model.User
import hudson.model.View
import jenkins.model.Jenkins
import jenkins.security.ApiTokenProperty
import org.custommonkey.xmlunit.XMLUnit
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.jvnet.hudson.test.JenkinsRule
import org.jvnet.hudson.test.MockAuthorizationStrategy
import org.jvnet.hudson.test.recipes.WithPlugin

/**
 * Test for the dslUpdateJenkins test. Uses {@link JenkinsRule} to create a Jenkins instance to run the tests against.
 */
class UpdateJenkinsTest extends AbstractTaskTest {

    @Rule
    private final JenkinsRule jenkinsRule = new JenkinsRule()

    def setup() {
        jenkinsRule.contextPath = '/jenkins'
    }

    def jenkinsUrlParam() {
        return "--jenkinsUrl=${jenkinsRule.URL.toExternalForm()}"
    }

    def gradleSectionOutput(String output, String section) {
        def lines = output.readLines()
        def indexBegin = lines.findIndexOf { it == section }
        def indexEnd = lines.findIndexOf(indexBegin) { it.empty }
        return lines.subList(indexBegin + 1, indexEnd)*.stripIndent()
    }

    def 'upload empty freestyle job'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-freestyle-job.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        Item item = jenkinsRule.jenkins.getItemByFullName('job')
        item instanceof FreeStyleProject
        XMLUnit.compareXML(
                readResource('updateJenkins/empty-freestyle-job.xml'),
                item.configFile.asString()
        ).identical()
    }

    def 'upload empty list view'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-list-view.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        View view = jenkinsRule.jenkins.getView('view')
        view instanceof ListView
        def output = new ByteArrayOutputStream()
        view.writeXml(output)
        XMLUnit.compareXML(
                readResource('updateJenkins/empty-list-view.xml'),
                output.toString()
        ).identical()
    }

    @WithPlugin('cloudbees-folder-6.1.0.hpi')
    def 'upload folder'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/folder.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        Item item = jenkinsRule.jenkins.getItemByFullName('folder')
        item instanceof Folder
        XMLUnit.compareXML(
                readResource('updateJenkins/folder.xml'),
                item.configFile.asString()
        ).identical()
    }

    @WithPlugin('cloudbees-folder-6.1.0.hpi')
    def 'upload job in folder'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/job-in-folder.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        Item item = jenkinsRule.jenkins.getItemByFullName('folder/job')
        item instanceof FreeStyleProject
    }

    @WithPlugin('cloudbees-folder-6.1.0.hpi')
    def 'upload view in folder'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/view-in-folder.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        Folder folder = jenkinsRule.jenkins.getItemByFullName('folder')
        View view = folder.getView('view')
        view instanceof ListView
    }

    @WithPlugin('cloudbees-folder-6.1.0.hpi')
    def 'filter applies to folders'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/filter-folders.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                '--filter=.*unfiltered.*'
        ).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        jenkinsRule.jenkins.getItemByFullName('folder-unfiltered') instanceof Folder
        jenkinsRule.jenkins.getItemByFullName('folder-unfiltered/subfolder') instanceof Folder
        jenkinsRule.jenkins.getItemByFullName('folder-filtered') == null
        jenkinsRule.jenkins.getItemByFullName('folder-filtered/subfolder') == null
    }

    @WithPlugin('cloudbees-folder-6.1.0.hpi')
    def 'filter applies to jobs'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/filter-jobs.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                '--filter=(folder|.*unfiltered.*)'
        ).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        jenkinsRule.jenkins.getItemByFullName('job-unfiltered') instanceof FreeStyleProject
        jenkinsRule.jenkins.getItemByFullName('folder/job-unfiltered') instanceof FreeStyleProject
        jenkinsRule.jenkins.getItemByFullName('job-filtered') == null
        jenkinsRule.jenkins.getItemByFullName('folder/job-filtered') == null
    }

    @WithPlugin('cloudbees-folder-6.1.0.hpi')
    def 'filter applies to views'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/filter-views.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                '--filter=(folder|.*unfiltered.*)'
        ).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        Folder folder = jenkinsRule.jenkins.getItemByFullName('folder')
        jenkinsRule.jenkins.getView('view-unfiltered') instanceof ListView
        folder.getView('view-unfiltered') instanceof ListView
        jenkinsRule.jenkins.getView('view-filtered') == null
        folder.getView('view-filtered') == null
    }

    def 'unchanged job is not uploaded'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-freestyle-job.groovy')
        gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        result.output.contains('UP-TO-DATE: 1')
        !result.output.contains('CREATED')
        !result.output.contains('UPDATED')
    }

    def 'dry run does not change jenkins'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-freestyle-job.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                '--dryRun'
        ).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        jenkinsRule.jenkins.getItemByFullName('job') == null
    }

    def 'task fails on compile error in DSL script'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        testProjectDir.newFile('src/jobdsl/jobdsl.groovy') << 'a'

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).buildAndFail()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.FAILED
    }

    def 'task fails when no DSL scripts are found'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).buildAndFail()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.FAILED
        result.output.contains('No files found in JobDSL source folder.')
    }

    def 'plugin checks fail without admin permission'() {
        given:
        def user = User.get('user', true)
        jenkinsRule.jenkins.authorizationStrategy = new MockAuthorizationStrategy()
        jenkinsRule.jenkins.securityRealm = jenkinsRule.createDummySecurityRealm()
        def apiToken = user.getProperty(ApiTokenProperty).apiToken

        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-freestyle-job.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                "--jenkinsUser=${user.id}",
                "--jenkinsApiToken=${apiToken}"
        ).buildAndFail()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.FAILED
        result.output.contains('Could not load list of plugins from Jenkins server')
        jenkinsRule.jenkins.getItemByFullName('job') == null
    }

    def 'plugin checks work with admin permission'() {
        given:
        def admin = User.get('admin', true)
        jenkinsRule.jenkins.authorizationStrategy = new MockAuthorizationStrategy().
                grant(Jenkins.ADMINISTER).everywhere().to(admin)
        jenkinsRule.jenkins.securityRealm = jenkinsRule.createDummySecurityRealm()
        def apiToken = admin.getProperty(ApiTokenProperty).apiToken

        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-freestyle-job.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                "--jenkinsUser=${admin.id}",
                "--jenkinsApiToken=${apiToken}"
        ).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        jenkinsRule.jenkins.getItemByFullName('job') instanceof FreeStyleProject
    }

    def 'disable plugin checks works without admin permission'() {
        given:
        def user = User.get('user', true)
        jenkinsRule.jenkins.authorizationStrategy = new MockAuthorizationStrategy().
                grant(Jenkins.READ).everywhere().to(user).
                grant(Item.CREATE).everywhere().to(user)
        jenkinsRule.jenkins.securityRealm = jenkinsRule.createDummySecurityRealm()
        def apiToken = user.getProperty(ApiTokenProperty).apiToken

        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/empty-freestyle-job.groovy')

        when:
        def result = gradleRunner.withArguments(
                'dslUpdateJenkins',
                jenkinsUrlParam(),
                "--jenkinsUser=${user.id}",
                "--jenkinsApiToken=${apiToken}",
                '--disablePluginChecks'
        ).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        jenkinsRule.jenkins.getItemByFullName('job') instanceof FreeStyleProject
    }

    @WithPlugin('groovy-1.30.hpi')
    def 'deprecated plugins are reported'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/deprecated-plugins.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        gradleSectionOutput(result.output, 'Deprecated plugins:') == ['groovy']
    }

    def 'missing plugins are reported'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/missing-plugins.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        gradleSectionOutput(result.output, 'Missing plugins:') == ['gradle', 'timestamper']
    }

    @WithPlugin('gradle-1.22.hpi')
    def 'outdated plugins are reported'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/outdated-plugins.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        gradleSectionOutput(result.output, 'Outdated plugins:') == ['gradle']
    }

    def 'groovy postbuild step with UTF-8 characters is uploaded correctly'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/groovy-postbuild-with-utf-8.groovy')

        when:
        def result = gradleRunner.withArguments('dslUpdateJenkins', jenkinsUrlParam()).build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS
        Item item = jenkinsRule.jenkins.getItemByFullName('job')
        item instanceof FreeStyleProject
        XMLUnit.compareXML(
                readResource('updateJenkins/groovy-postbuild-with-utf-8.xml'),
                item.configFile.asString()
        ).identical()
    }

    def 'job with generated DSL is uploaded correctly'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build-with-generated-dsl.gradle')
        copyResourceToTestDir('updateJenkins/job-with-generated-dsl.groovy')

        when:
        def result = gradleRunner
                .withArguments('dslUpdateJenkins', jenkinsUrlParam())
                .build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS

        Item item = jenkinsRule.jenkins.getItemByFullName('job')
        item instanceof FreeStyleProject
        XMLUnit.compareXML(
                readResource('updateJenkins/job-with-generated-dsl.xml'),
                item.configFile.asString()
        ).identical()
    }

    def 'task fails when plugin for generated DSL is missing'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/job-with-generated-dsl.groovy')

        when:
        def result = gradleRunner
                .withArguments('dslUpdateJenkins', jenkinsUrlParam())
                .buildAndFail()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.FAILED
        result.output.contains('No signature of method: javaposse.jobdsl.dsl.helpers.ScmContext.cvsscm() is ' +
                'applicable for argument types')
    }

    def 'job with Job DSL extension is uploaded correctly'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build-with-jobdsl-extension.gradle')
        copyResourceToTestDir('updateJenkins/job-with-job-dsl-extension.groovy')

        when:
        def result = gradleRunner
                .withArguments('dslUpdateJenkins', jenkinsUrlParam())
                .build()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.SUCCESS

        Item item = jenkinsRule.jenkins.getItemByFullName('job')
        item instanceof FreeStyleProject
        XMLUnit.compareXML(
                readResource('updateJenkins/job-with-job-dsl-extension.xml'),
                item.configFile.asString()
        ).identical()
    }

    def 'task fails when plugin for Job DSL extension is missing'() {
        given:
        buildFile << readBuildGradle('updateJenkins/build.gradle')
        copyResourceToTestDir('updateJenkins/job-with-job-dsl-extension.groovy')

        when:
        def result = gradleRunner
                .withArguments('dslUpdateJenkins', jenkinsUrlParam())
                .buildAndFail()

        then:
        result.task(':dslUpdateJenkins').outcome == TaskOutcome.FAILED
        result.output.contains(
                'No signature of method: javaposse.jobdsl.dsl.helpers.publisher.PublisherContext.jgivenReports() is ' +
                        'applicable for argument types')
    }

}
