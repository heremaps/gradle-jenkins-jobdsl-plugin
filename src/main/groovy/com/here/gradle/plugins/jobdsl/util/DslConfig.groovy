package com.here.gradle.plugins.jobdsl.util

class DslConfig {

    private static Map<String, ?> configuration

    def static get(String key) {
        return configuration[key]
    }

    def static setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration
    }

}
