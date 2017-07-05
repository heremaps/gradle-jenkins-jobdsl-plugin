package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslFactory

/**
 * Implementation of {@link JobBuilder2} that can be used with a {@link PipelineBuilder}. This class adds support for
 * {@link PipelineBuilder#baseFolders} by overriding the required methods.
 */
class PipelineJobBuilder extends JobBuilder2 {

    PipelineBuilder pipelineBuilder

    PipelineJobBuilder(DslFactory dslFactory) {
        super(dslFactory)
    }

    List<String> allFolders() {
        return pipelineBuilder?.baseFolders ? [*pipelineBuilder.baseFolders, *folders] : [*folders]
    }

    @Override
    String fullJobName() {
        return (allFolders() + name).join('/')
    }

}
