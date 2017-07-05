package com.here.gradle.plugins.jobdsl.tasks

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Common code for tests of plugin tasks.
 */
class AbstractTaskTest extends Specification {

    @Rule
    protected final TemporaryFolder testProjectDir = new TemporaryFolder()

    protected File buildFile
    protected GradleRunner gradleRunner

    def classpathString() {
        def classpath = gradleRunner
                .pluginClasspath
                .collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(', ')
        return classpath
    }

    def readResource(String path) {
        getClass().classLoader.getResource(path).text
    }

    def readBuildGradle(String path) {
        readResource(path).replace('CLASSPATH_STRING', classpathString())
    }

    def copyResourceToTestDir(String from, String to = 'src/jobdsl/jobdsl.groovy') {
        testProjectDir.newFile(to) << readResource(from)
    }

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        testProjectDir.newFolder('src', 'jobdsl')

        gradleRunner = GradleRunner.create().withProjectDir(testProjectDir.root).withPluginClasspath()
    }

}
