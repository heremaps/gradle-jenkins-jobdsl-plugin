package com.here.gradle.plugins.jobdsl.util

import javaposse.jobdsl.dsl.DslFactory

/**
 * A {@link JobBuilder} that creates a seed job that runs the dslGenerateXml Gradle task to create the XML files and
 * then uses a system groovy script build step to update the jobs. This is a replacement for using the dslUpdateJenkins
 * task in a seed job and promises a much better performance because it uses the internal Jenkins API instead of the
 * REST API.<br>
 * The builder does not provide a default way to get the files of the Gradle project, this has to be added by calling
 * {@link JobBuilder#addDsl(groovy.lang.Closure)}. Usually this will be a copy artifacts step or a SCM configuration.
 */
class GroovySeedJobBuilder extends JobBuilder {

    /**
     * Provide the script that will execute the dslGenerateXml Gradle task for your project. It defaults to the basic
     * "./gradlew --no-daemon dslGenerateXml" which will only work if your project is present in the workspace directory
     * and does not use server configuration.<br>
     * Set this to null if you get the XML files in another way, for example by copying them from another job.
     */
    String generateXmlScript = './gradlew --no-daemon dslGenerateXml'

    /**
     * The path on Jenkins master to use as temporary folder for the XML files generated on the node. The have to be
     * copied to the Jenkins master in order to apply them. The build number of the seed job will be added to the
     * prefix, also the job will fail when the folder already exists and delete it after generating the jobs.
     */
    String temporaryXmlDirPrefix = '/tmp/seedjob'

    /**
     * The base directory where the generated XML files are located. Usually this is the build/jobdsl/xml folder in the
     * Gradle project. This value is used by the default {@link #seedJobGroovyScript}, if you change the script you have
     * to include the string XML_BASE_DIR_PLACEHOLDER which will be replaced by xmlBaseDir automatically.
     */
    String xmlBaseDir = 'build/jobdsl/xml'

    /**
     * The Groovy script that updates the jobs from the XML files, executed in a System Groovy Script build step.<br>
     * All items (folders and jobs) are compared and only updated when the XML has changed. Folders that contain views
     * will always be updated.<br>
     * Views in the Jenkins root will always be updated, views inside folders will always be re-created.<br>
     * <em>Only change this value if you know what you are doing!</em>
     */
    String seedJobGroovyScript = getClass().classLoader.getResource('seedJobGroovyScript.groovy').text

    GroovySeedJobBuilder(DslFactory dslFactory) {
        super(dslFactory)

        addDsl {
            steps {
                if (generateXmlScript) {
                    shell(generateXmlScript)
                }

                systemGroovyCommand(seedJobGroovyScript.
                        replaceAll('XML_BASE_DIR_PLACEHOLDER', xmlBaseDir).
                        replaceAll('TEMPORARY_XML_DIR_PREFIX_PLACEHOLDER', temporaryXmlDirPrefix))
            }
        }
    }

}
