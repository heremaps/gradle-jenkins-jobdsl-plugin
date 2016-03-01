package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslException

class DslConfig {

    private static Map<String, ?> configuration
    private static Map<String, ?> serverConfiguration

    /**
     * Get a value from the configuration map. Values from the plain configuration override values from the server
     * configuration.
     *
     * @param key
     * @return
     */
    def static get(String key) {
        if (configuration.containsKey(key)) {
            return configuration[key]
        } else if (serverConfiguration.containsKey(key)) {
            return serverConfiguration[key]
        } else {
            throw new DslException("Required configuration '${key}' does not exist.")
        }
    }

    def static setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration
    }

    def static setServerConfiguration(Map<String, ?> serverConfiguration) {
        this.serverConfiguration = serverConfiguration
    }

}
