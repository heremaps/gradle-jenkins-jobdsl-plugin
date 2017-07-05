package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.Job

class PipelineBuilder {

    List<PipelineJobBuilder> jobBuilders = []
    List<String> baseFolders
    Map<String, Object> defaultConfiguration = [:]
    Closure commonDsl = { }

    void addJob(PipelineJobBuilder jobBuilder) {
        jobBuilder.pipelineBuilder = this
        jobBuilders.add(jobBuilder)
    }

    void defaultConfiguration(Map<String, Object> defaultConfiguration) {
        this.defaultConfiguration = defaultConfiguration
    }

    void commonDsl(@DelegatesTo(Job) Closure commonDsl) {
        this.commonDsl = commonDsl
    }

    void baseFolders(List<String> baseFolders) {
        this.baseFolders = [*baseFolders]
    }

    void build() {
        jobBuilders.each { jobBuilder ->
            applyDefaultConfiguration(jobBuilder)
            applyCommonDsl(jobBuilder)
            jobBuilder.build()
        }
    }

    void applyDefaultConfiguration(PipelineJobBuilder jobBuilder) {
        defaultConfiguration.each { key, value ->
            if (jobBuilder.hasProperty(key)) {
                jobBuilder."${key}" = value
            } else {
                println "Ignoring default configuration '${key}' for job '${jobBuilder.fullJobName()}'"
            }
        }
    }

    void applyCommonDsl(PipelineJobBuilder jobBuilder) {
        jobBuilder.addDsl(commonDsl)
    }

}
