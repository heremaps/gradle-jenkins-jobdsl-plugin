package com.here.gradle.plugins.jobdsl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * The configuration extension for the Job DSL plugin.
 */
class JobDslPluginExtension {

    Map<String, ?> configuration = [:]
    NamedDomainObjectContainer<ServerDefinition> servers

    JobDslPluginExtension(Project project) {
        servers = project.container(ServerDefinition)
    }

    def servers(Closure closure) {
        servers.configure(closure)
    }

}
