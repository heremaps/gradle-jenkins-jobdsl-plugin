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
