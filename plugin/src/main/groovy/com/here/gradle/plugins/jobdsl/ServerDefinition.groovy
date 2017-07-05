package com.here.gradle.plugins.jobdsl

/**
 * Class holding the configuration for a Jenkins server.
 */
class ServerDefinition {

    String name
    String jenkinsUrl
    String jenkinsUser
    String jenkinsApiToken
    Map<String, ?> configuration = [:]

    ServerDefinition(String name) {
        this.name = name
    }

}
