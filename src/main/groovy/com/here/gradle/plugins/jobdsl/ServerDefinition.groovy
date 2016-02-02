package com.here.gradle.plugins.jobdsl

class ServerDefinition {

    String name
    String jenkinsUrl
    String jenkinsUser
    String jenkinsApiToken

    ServerDefinition(String name) {
        this.name = name
    }

}
