package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslFactory

class PipelineJobBuilder extends JobBuilder2 {

    PipelineBuilder pipelineBuilder

    PipelineJobBuilder(DslFactory dslFactory) {
        super(dslFactory)
    }

    List<String> allFolders() {
        return [*pipelineBuilder.baseFolders, *folders]
    }

    @Override
    String fullJobName() {
        return (allFolders() + name).join('/')
    }

}
