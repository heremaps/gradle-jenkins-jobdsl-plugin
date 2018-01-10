package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslFactory
import org.codehaus.groovy.runtime.ComposedClosure
import spock.lang.Specification

/**
 * Tests for the {@link com.here.gradle.plugins.jobdsl.util.PipelineBuilder}.
 */
class PipelineBuilderSpec extends Specification {

    def 'jobs added to pipeline builder are built'() {
        given:
        def dslFactory = Mock(DslFactory)

        def freeStyleJob = new PipelineJobBuilder(dslFactory)
        freeStyleJob.name = 'freeStyleJob'
        freeStyleJob.freeStyleJob { }

        def matrixJob = new PipelineJobBuilder(dslFactory)
        matrixJob.name = 'matrixJob'
        matrixJob.matrixJob { }

        def pipelineBuilder = new PipelineBuilder()

        when:
        pipelineBuilder.addJob(freeStyleJob)
        pipelineBuilder.addJob(matrixJob)
        pipelineBuilder.build()

        then:
        1 * dslFactory.freeStyleJob('freeStyleJob', _ as ComposedClosure)
        1 * dslFactory.matrixJob('matrixJob', _ as ComposedClosure)
        0 * _
    }

    def 'default configuration is applied'() {
        given:
        def dslFactory = Mock(DslFactory)

        def job1 = new TestPipelineJobBuilder(dslFactory, null)
        job1.name = 'job1'
        job1.freeStyleJob { }

        def job2 = new TestPipelineJobBuilder(dslFactory, 'custom value')
        job2.name = 'job2'
        job2.freeStyleJob { }

        def pipelineBuilder = new PipelineBuilder()
        pipelineBuilder.defaultConfiguration([prop: 'default value'])

        when:
        pipelineBuilder.addJob(job1)
        pipelineBuilder.addJob(job2)
        pipelineBuilder.build()

        then:
        job1.prop == 'default value'
        job2.prop == 'custom value'
    }

    def 'common DSL is applied'() {
        given:
        def dslFactory = Mock(DslFactory)

        def job = new PipelineJobBuilder(dslFactory)
        job.name = 'job'
        job.freeStyleJob { }

        def pipelineBuilder = new PipelineBuilder()

        def commonDsl = { }
        pipelineBuilder.commonDsl(commonDsl)

        when:
        pipelineBuilder.addJob(job)
        pipelineBuilder.build()

        then:
        job.dslClosures.contains(commonDsl)
    }

    def 'base folders are applied'() {
        given:
        def dslFactory = Mock(DslFactory)

        def job = new PipelineJobBuilder(dslFactory)
        job.name = 'job'
        job.folders = ['jobFolder1', 'jobFolder2']
        job.freeStyleJob { }

        def pipelineBuilder = new PipelineBuilder()
        pipelineBuilder.baseFolders(['baseFolder1', 'baseFolder2'])

        when:
        pipelineBuilder.addJob(job)

        then:
        job.fullJobName() == 'baseFolder1/baseFolder2/jobFolder1/jobFolder2/job'
    }

    private class TestPipelineJobBuilder extends PipelineJobBuilder {
        String prop

        TestPipelineJobBuilder(DslFactory dslFactory, String prop) {
            super(dslFactory)
            this.prop = prop
        }
    }

}
