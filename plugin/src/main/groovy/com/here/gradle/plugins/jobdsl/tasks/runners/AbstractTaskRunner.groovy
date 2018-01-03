package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.DeferredJobManagement
import com.here.gradle.plugins.jobdsl.GradleJobDslPluginException
import com.here.gradle.plugins.jobdsl.ItemFilter
import com.here.gradle.plugins.jobdsl.util.DslConfig

import groovy.json.JsonSlurper

import javaposse.jobdsl.dsl.DslScriptLoader
import javaposse.jobdsl.dsl.GeneratedItems
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.ScriptRequest

import jenkins.model.Jenkins

import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.jvnet.hudson.test.JenkinsRule

/**
 * Common code for all tasks that extend {@link com.here.gradle.plugins.jobdsl.tasks.AbstractDslTask} to perform their
 * actions in another process. This class takes care of receiving and decoding the configuration options for the tasks.
 */
abstract class AbstractTaskRunner {

    public static final int EXIT_SUCCESS = 0
    public static final int EXIT_FAILURE = 1
    public static final int EXIT_EXCEPTION = 2

    protected JobManagement jobManagement
    protected Properties runProperties

    @SuppressWarnings('CatchException')
    @SuppressWarnings('Instanceof')
    @SuppressWarnings('PrintStackTrace')
    @SuppressWarnings('SystemExit')
    void run() {
        runProperties = System.properties

        def slurper = new JsonSlurper()

        def configuration = slurper.parseText(decodeBase64(runProperties['configuration'].toString()))
        DslConfig.configuration = configuration

        def serverConfiguration = runProperties['serverConfiguration'].toString().length() == 0 ?
                [:] : slurper.parseText(decodeBase64(runProperties['serverConfiguration'].toString()))
        DslConfig.serverConfiguration = serverConfiguration

        def filter = new ItemFilter(decodeBase64(runProperties['filter']))

        def jenkinsRule = new JenkinsRule()
        jenkinsRule.contextPath = '/jenkins'

        def description = Description.createSuiteDescription('Run Job DSL Task', [])

        def statement = new Statement() {
            @Override
            void evaluate() throws Throwable {
                def plugins = jenkinsRule.pluginManager.plugins.collect { "${it.shortName}:${it.version}" }
                println("Installed plugins: $plugins")

                jobManagement = createJobManagement(jenkinsRule.jenkins, filter)
                DslScriptLoader loader = new DslScriptLoader(jobManagement)

                if (!runProperties['inputFiles']) {
                    throw new GradleJobDslPluginException('No files found in JobDSL source folder.')
                }

                List<GeneratedItems> generatedItems = []

                runProperties['inputFiles'].split(File.pathSeparator).each { String filename ->
                    println "Loading ${filename}"
                    ScriptRequest scriptRequest = new ScriptRequest(new File(filename).getText('UTF-8'))
                    generatedItems += loader.runScripts([scriptRequest])
                }

                if (jobManagement instanceof DeferredJobManagement) {
                    jobManagement.applyChanges()
                }

                postProcess(jenkinsRule.jenkins, generatedItems, filter)
            }
        }

        try {
            jenkinsRule.apply(statement, description).evaluate()
            System.exit(EXIT_SUCCESS)
        } catch (Exception e) {
            e.printStackTrace()
            System.exit(EXIT_EXCEPTION)
        } finally {
            System.exit(EXIT_FAILURE)
        }
    }

    abstract JobManagement createJobManagement(Jenkins jenkins, ItemFilter filter)

    @SuppressWarnings('EmptyMethodInAbstractClass')
    @SuppressWarnings('UnusedMethodParameter')
    void postProcess(Jenkins jenkins, List<GeneratedItems> generatedItems, ItemFilter filter) {
        // do nothing by default
    }

    private String decodeBase64(String encoded) {
        new String(encoded.decodeBase64())
    }

}
