package com.here.gradle.plugins.jobdsl

/**
 * Utility class to check if item names match a regular expression.
 */
class ItemFilter {

    private final String pattern

    ItemFilter(String pattern) {
        this.pattern = pattern
    }

    /**
     * Check if the itemName matches the filter pattern. If the pattern is null or empty this always returns true.
     *
     * @param itemName Name of the Jenkins job or view.
     * @return True if filter is null or empty or itemName matches filter pattern.
     */
    boolean matches(String itemName) {
        pattern ? itemName ==~ pattern : true
    }

}
