# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## 3.7.0 (2019-09-18)

- [#120](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/120) - Disable the timeout of JenkinsRule when
  generating job configurations in `dslGenerateXml` and `dslUpdateJenkins`. Before that change it was not possible to
  use the plugin for larger setups that take longer to generate than the default timeout of 180 seconds.
- [#126](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/126) - Upgrade job-dsl-core to 1.76.

## 3.6.0 (2019-09-17)

- [#122](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/122) - Add an option to ignore SSL errors.

## 3.5.0 (2019-06-25)

- [#114](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/114) - Fix support for task options in Gradle 5.

## 3.4.0 (2019-04-12)

- [#110](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/110) - Add Proxy support for the update Jenkins
  task. Authentication is not supported.

## 3.3.0 (2019-03-27)

- Upgrade job-dsl-core to 1.72.

## 3.2.3 (2019-01-21)

- Fix the published POM file to include a license and SCM information.

## 3.2.2 (2019-01-21)

- Upgrade the Gradle plugin-publish plugin to 0.10.0 as this version should now include the POM modifications in the
  published POM file.

## 3.2.1 (2018-07-05)

- [#93](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/93) - Make sure all files are closed in the seed
  job script.

## 3.2.0 (2018-06-28)

- Upgrade job-dsl-core to 1.69.
- [#78](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/78) - Improve handling of temporary folders in
  seed job script.
- [#79](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/79) - Fix handling of generated XML files that
  start with an `<?xml ..>` tag.
- [#82](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/82) - Add the option `failOnMissingPlugin` to
  the dslGenerateXml task.
- [#83](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/83) - Make the seed job script more resilient to
  errors when creating/updating jobs/views.
- [#86](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/86) - Inherit folder properties from existing
  folders: Some folder properties like credentials which are configured in the Jenkins UI are stored in the folder XML
  file. Keep these values if they do not exist in the generated XML.
- [#87](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/pull/87) - Improve error reporting in seed job.

## 3.1.0 (2018-02-07)

- Upgrade job-dsl-core to 1.67.
- Process Job DSL scripts in specific order. Scripts from the same folder are processed in alphabetical order and before
  any scripts from subfolders. Subfolders are also processed in alphabetical order.
- Fix [#65](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/issues/65) - PipelineBuilder: Add common DSL
  Closure only once when PipelineBuilder.build() is called multiple times.

## 3.0.0 (2018-01-10)

- Fix [#12](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/issues/12) - Add support for auto-generated DSL and
  Job DSL extensions by executing the Job DSL scripts in a local Jenkins instance.
- Fix [#63](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/issues/63) - PipelineBuilder: Do not overwrite
  values with default configuration.

## 2.1.1 (2017-12-04)

- RestJobManagement: Use UTF-8 also on Windows. Before configurations containing UTF-8 characters got corrupted by the
  dslUpdateJenkins task when using Windows.

## 2.1.0 (2017-10-20)

- Upgrade job-dsl-core to 1.66

## 2.0.0 (2017-08-03)

- First public release published under the Apache-2.0 license
- Rename plugin to gradle-jenkins-jobdsl-plugin
- Rename JobBuilder2 to JobBuilder, replacing the original JobBuilder

## 1.0.32 (2017-05-23)

- Upgrade job-dsl-core to 1.61
- Server specific configuration now overrides global configuration with the same key

## 1.0.31 (2016-12-16)

- Add support for crumbs for Jenkins with enabled CSRF protection.

## 1.0.30 (2016-11-17)

- Fix a bug when executing DSL tasks on Windows.

## 1.0.29 (2016-08-25)

- Improve performance of the Groovy script used in GroovySeedJobBuilder introduced in 1.0.28

## 1.0.28 (2016-08-18)

- New GroovySeedJobBuilder, can be used to create a seed job that runs in a System Groovy build step instead of using
  the REST API. This has a much better performance because it removes the network overhead.
- Add "HERE Proprietary" license to Maven POM
- JobBuilder2: Provide default empty closure for job type methods
- Publish source and Groovydoc artifacts to Maven repository
- Add some functional tests for the plugin
- Upgrade job-dsl-core from 1.45 to 1.49
    - For changelog see https://github.com/jenkinsci/job-dsl-plugin/wiki#release-notes
- Improve error message when config key is not found in DslConfig

## 1.0.27 (2016-06-07)

- Add option --disablePluginChecks to dslUpdateJenkins task
    - This is necessary because on recent Jenkins releases the Overall/Administer permission is required to fetch the
      plugin list. See [SECURITY-250](https://wiki.jenkins-ci.org/display/SECURITY/Jenkins+Security+Advisory+2016-05-11)

## 1.0.26 (2016-06-03)

- Fix bug with closure being executed in wrong order in JobBuilder2
- Fix NullPointerException in PipelineJobBuilder
- Set pipelineBuilder field in PipelineJobBuilder when adding them to a PipelineBuilder

## 1.0.25 (2016-05-17)

- Fix bug in writing views in folders in dslGenerateXml
- Fix job name generation in JobBuilder2 and PipelineJobBuilder
- Forbid using '/' in job names in JobBuilder2
- Add some convenience methods to add DSL code in JobBuilder2

## 1.0.24 (2016-05-11)

- Add support for managing pipelines
    - New PipelineBuilder class
    - New JobBuilder2 implementation

## 1.0.23 (2016-04-25)

- Upgrade job-dsl-core to 1.45
    - Release notes: https://github.com/jenkinsci/job-dsl-plugin/wiki#release-notes
    - Migration guide: https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration
- Do not require trailing slash at Jenkins URL
- Upload changes in defined order to make sure folders are created before the jobs inside them
    - First upload all folders
    - Then upload all jobs
    - Finally upload all views
- Minor bug fixes

## 1.0.22 (2016-04-14)

- Add support for views inside folders
- Do not require Gradle as a dependency for projects using the plugin

## 1.0.21 (2016-04-11)

- Add dry run option for dslUpdateJenkins task
- Improve logging
    - Remove some verbose logs
    - Print summary after dslUpdateJenkins
- Fail dslUpdateJenkins task when not all items/views could be updated
- Fix bug with processing plugin list from server

## 1.0.20 (2016-03-16)

- Add filter option to tasks to select which jobs are processed
- Do not update jobs when the XML has not changed

## 1.0.19 (2016-03-09)

- Upgrade job-dsl-core to 1.43
- Fix Windows related bugs

## 1.0.17 (2016-02-16)

- Improve log output

## 1.0.15 (2016-02-15)

- Add server specific configuration

## 1.0.14 (2016-02-02)

- Add support to configure multiple servers in the build file
- Add configuration map

## 1.0.13 (2016-02-02)

- Improve error logging
- Rename jenkinsPassword to jenkinsApiToken

## 1.0.12 (2016-02-01)

- Initial release
