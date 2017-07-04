package com.here.gradle.plugins.jobdsl

import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.MemoryJobManagement
import javaposse.jobdsl.dsl.NameNotProvidedException

class FilteringMemoryJobManagement extends MemoryJobManagement {

    private final ItemFilter filter

    public FilteringMemoryJobManagement(ItemFilter filter) {
        this.filter = filter
    }

    @Override
    boolean createOrUpdateConfig(Item item, boolean ignoreExisting) throws NameNotProvidedException {
        filter.matches(item.name) ? super.createOrUpdateConfig(item, ignoreExisting) : true
    }

    @Override
    void createOrUpdateView(String viewName, String config, boolean ignoreExisting) {
        if (filter.matches(viewName)) {
            super.createOrUpdateView(viewName, config, ignoreExisting)
        }
    }

}
