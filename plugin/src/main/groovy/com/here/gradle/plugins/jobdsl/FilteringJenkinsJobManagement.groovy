package com.here.gradle.plugins.jobdsl

import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.NameNotProvidedException
import javaposse.jobdsl.plugin.JenkinsJobManagement

/**
 * Extends {@link JenkinsJobManagement} to add support for an {@link ItemFilter}.
 */
class FilteringJenkinsJobManagement extends JenkinsJobManagement {

    private final ItemFilter filter

    FilteringJenkinsJobManagement(ItemFilter filter, PrintStream outputLogger, Map<String, ?> envVars, File workspace) {
        super(outputLogger, envVars, workspace)
        this.filter = filter
    }

    @Override
    boolean createOrUpdateConfig(Item dslItem, boolean ignoreExisting) throws NameNotProvidedException {
        filter.matches(dslItem.name) ? super.createOrUpdateConfig(dslItem, ignoreExisting) : true
    }

    @Override
    void createOrUpdateView(String path, String config, boolean ignoreExisting) {
        if (filter.matches(path)) {
            super.createOrUpdateView(path, config, ignoreExisting)
        }
    }

}
