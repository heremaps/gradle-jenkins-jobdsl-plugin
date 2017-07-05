package com.here.gradle.plugins.jobdsl

import spock.lang.Specification

/**
 * Tests for the {@link ItemFilter}.
 */
class ItemFilterSpec extends Specification {

    def 'filter is ignored when null'() {
        setup:
        ItemFilter filter = new ItemFilter(null)

        expect:
        filter.matches('name') == true

    }

    def 'filter is ignored when empty'() {
        setup:
        ItemFilter filter = new ItemFilter('')

        expect:
        filter.matches('name') == true
    }

    def 'regular expression is working'() {
        setup:
        ItemFilter filter = new ItemFilter('^Test(Job|View)')

        expect:
        filter.matches('Test') == false
        filter.matches(' TestJob') == false
        filter.matches('TestJob') == true
        filter.matches('TestView') == true
    }

}
