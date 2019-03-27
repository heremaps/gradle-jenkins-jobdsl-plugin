package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslException

/**
 * This class holds the global and server specific configuration maps.
 */
class DslConfig {

    private static Map<String, ?> configuration
    private static Map<String, ?> serverConfiguration

    /**
     * Get a value from the configuration map. Values from the plain configuration override values from the server
     * configuration.
     *
     * @param key
     */
    static get(String key) {
        if (serverConfiguration.containsKey(key)) {
            return serverConfiguration[key]
        } else if (configuration.containsKey(key)) {
            return configuration[key]
        } else {
            throw new DslException("""\
                Required configuration '${key}' does not exist. Check if it is defined in the Gradle build file. Maybe \
                it is a server specific configuration and you forgot to provide the --server argument.\
                """.stripIndent())
        }
    }

    static setConfiguration(Map<String, ?> configuration) {
        this.configuration = configuration
    }

    static setServerConfiguration(Map<String, ?> serverConfiguration) {
        this.serverConfiguration = serverConfiguration
    }

}
