package com.here.gradle.plugins.jobdsl

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

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
