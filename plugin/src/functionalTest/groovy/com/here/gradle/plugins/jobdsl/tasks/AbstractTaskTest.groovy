/*
 * Copyright (C) 2016-2019 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * License-Filename: LICENSE
 */

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
        getClass().classLoader.getResource(path).getText('UTF-8')
    }

    def readBuildGradle(String path) {
        readResource(path).replace('CLASSPATH_STRING', classpathString()).getBytes('UTF-8')
    }

    def copyResourceToTestDir(String from, String to = 'src/jobdsl/jobdsl.groovy') {
        testProjectDir.newFile(to) << readResource(from).getBytes('UTF-8')
    }

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        testProjectDir.newFolder('src', 'jobdsl')

        gradleRunner = GradleRunner.create().withProjectDir(testProjectDir.root).withPluginClasspath()
    }

}
