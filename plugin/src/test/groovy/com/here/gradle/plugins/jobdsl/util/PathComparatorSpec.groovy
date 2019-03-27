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

package com.here.gradle.plugins.jobdsl.util

import spock.lang.Specification

/**
 * Tests for the {@link com.here.gradle.plugins.jobdsl.util.PathComparator}.
 */
class PathComparatorSpec extends Specification {

    def "files in same folder are ordered alphabetically"() {
        given:
        def list = ['c', 'a', 'd', 'b']

        when:
        list.sort(new PathComparator())

        then:
        list.join(', ') == ['a', 'b', 'c', 'd'].join(', ')
    }

    def "files in subfolders come after files in parent folders"() {
        given:
        def list = ['a', 'a/a', 'a/b', 'a/a/a', 'b', 'b/a', 'b/a/a', 'c']

        when:
        list.sort(new PathComparator())

        then:
        list.join(', ') == ['a', 'b', 'c', 'a/a', 'a/b', 'a/a/a', 'b/a', 'b/a/a'].join(', ')
    }

}
