# Gradle Job DSL Plugin

This is a plugin to manage Jenkins (Job DSL)[https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin] projects in a
Gradle project.

## Contents

- [Usage](#Usage)
    - [Apply Plugin](#Apply-Plugin)
    - [Writing Job DSL Scripts](#Writing-Job-DSL-Scripts)
        - [Helper Classes](#Helper-Classes)
        - [Job Builders](#Job-Builders)
        - [Thirdparty Libraries](#Thirdparty-Libraries)
    - [Generate XML](#Generate-XML)
    - [Upload Jobs to Jenkins](#Upload-Jobs-to-Jenkins)
    - [Configure Servers](#Configure-Servers)
    - [Seed Job](#Seed-Job)
    - [Custom Configuration](#Custom-Configuration)
    - [Filter Jobs](#Filter-Jobs)
- [Development](#Development)
    - [Build the Plugin](#Build-the-Plugin)
    - [Test the Plugin](#Test-the-Plugin)
    - [Release the Plugin](#Release-the-Plugin)
- [Known Bugs](#Known-Bugs)
- [Release Notes](#Release-Notes)
- [License](#License)

## Usage

The plugin can be used to locally generate XML files for the jobs and to upload the generated jobs to a Jenkins
instance.

### Apply Plugin

To apply the plugin you need to add the necessary Maven repositories to the build script. The minimum build script looks
like this:

```groovy
def jobDslPluginVersion = '1.0.19'

buildscript {
    repositories {
        maven {
            url '[not yet published, URL will be added soon]'
        }
        jcenter()
        maven {
            url 'http://repo.jenkins-ci.org/releases/'
        }
    }
    dependencies {
        classpath "com.here.gradle.plugins:gradle-jobdsl-plugin:${jobDslPluginVersion}"
    }
}

apply plugin: 'com.here.jobdsl'

repositories {
    maven {
        url '[not yet published, URL will be added soon]'
    }
    jcenter()
    maven {
        url 'http://repo.jenkins-ci.org/releases/'
    }
}

dependencies {
    compile localGroovy()
    compile "com.here.gradle.plugins:gradle-jobdsl-plugin:${jobDslPluginVersion}"
}
```

### Writing Job DSL Scripts

By default all \*.groovy files from src/jobdsl and its subdirectories are evaluated as Job DSL scripts. You can change
the location by configuring the source set:

```groovy
sourceSets {
    jobdsl {
        groovy {
            srcDirs 'myCustomSrcFolder'
        }
    }
}
```

#### Helper Classes

You can add Groovy or Java classes to the respective source directories `src/main/groovy` and `src/main/java` and use
those classes in your Job DSL scripts, because they are automatically added to the classpath.

#### Job Builders

The recommended way to implement job templates are job builder classes. The plugin provides a basic `JobBuilder` class
that can be extended to create templates. Below you can find an example of how to create a template that automatically
enables the timestamp plugin for all your jobs:

```groovy
import com.here.gradle.plugins.jobdsl.util.JobBuilder
import javaposse.jobdsl.dsl.Job

class CustomJobBuilder extends JobBuilder {

    @Override
    Job build(Class<? extends Job> jobClass, @DelegatesTo(Job.class) Closure closure) {
        def job = super.build(jobClass, closure)
        job.with {
            wrappers {
                timestamps()
            }
        }
        return job
    }

}
```

It is important to keep the `@DelegatesTo` annotation to enable code completion support in your IDE.

To use the template you have to create an instance of your custom job builder class. The `JobBuilder` provides
convenience methods like `buildFreeStyleJob` or `buildMatrixJob` for each job type that optimize IDE support compared to
using the generic `build` method.

```groovy
new CustomJobBuilder(
        dslFactory: this,
        name: 'MyExampleJob'
).buildFreeStyleJob {

    description 'My example job'

    steps {
        shell('Hello World!')
    }
}
```

#### Thirdparty Libraries

To use thirdparty libraries in your Job DSL scripts add them to the dependencies closure of the build script. For
example the Job DSL plugin itself is added there to be able to use the `JobBuilder` class. This example shows how to add
the Google Guava library:

```groovy
dependencies {
    compile localGroovy()
    compile "com.here.gradle.plugins:gradle-jobdsl-plugin:${jobDslPluginVersion}"
    compile 'com.google.guava:guava:19.0'
}
```

### Generate XML

```bash
./gradlew dslGenerateXml
```

The XML files will be generated in build/jobdsl/.

### Upload Jobs to Jenkins

```bash
# Upload jobs to a server without authorization
./gradlew dslUpdateJenkins --jenkinsUrl=http://localhost:8080/

# Upload jobs to a server with authorization
./gradlew dslUpdateJenkins --jenkinsUrl=http://localhost:8080/ --jenkinsUser=yourUsername --jenkinsApiToken=yourApiToken

# Do a dry run without changing any jobs
./gradlew dslUpdateJenkins --jenkinsUrl=http://localhost:8080/ --dryRun
```

### Configure Servers

You can configure the different server you use in the build script instead of providing the configuration on the command
line:

```groovy
jobdsl {
    servers {
        localhost {
            jenkinsUrl = 'http://localhost:8080/'
        }

        staging {
            jenkinsUrl = 'http://your.jenkins.url:8080/'
            jenkinsUser = 'yourUsername'
            jenkinsApiToken = 'yourApiToken'
        }
    }
}
```

To use this configuration you have to select the server on the command line. You can override the configuration on the
command line, e.g. when you don't want to put your credentials in the build script:

```bash
# Use the localhost configuration
./gradlew dslUpdateJenkins --server=localhost

# Override the user and API token from the configuration
./gradlew dslUpdateJenkins --server=staging --jenkinsUser=yourOtherUsername --jenkinsApiToken=yourOtherApiToken
```

If you want each team member to use their own credentials the
 [gradle-credentials-plugin](https://github.com/etiennestuder/gradle-credentials-plugin) provides a nice way to let the
team members store their credentials encrypted without having to put them in the build script or on the command line
every time.

### Seed Job

To create a seed job similar to the Job DSL seed jobs (see
[Using Job DSL](https://github.com/jenkinsci/job-dsl-plugin/wiki/Tutorial---Using-the-Jenkins-Job-DSL)) just create a
freestyle job and run the Gradle command in a shell build step. Note that this will still use the REST API instead of
the internal Jenkins API to create the jobs.

Since version 1.0.28 you can use the `GroovySeedJobBuilder` to create a seed job that runs in a System Groovy build step
instead of using the REST API. This provides much better performance.

### Custom Configuration

There are use cases where you want to change a part of your job configuration on the command line. For example you might
want to create all jobs disabled on your localhost, and only enable them by default on your production Jenkins instance.
For this the plugin supports a configuration map that is forwarded to the Job DSL scripts. The example below shows how
to add a parameter to enable or disable all jobs:

```groovy
jobdsl {
    configuration = [
        enableJobs: project.properties['enableJobs'].toBoolean()
    ]
}
```

The values from the configuration map can be accessed from the Job DSL scripts by calling `DslConfig.get('key')`. The
adapted build method of the `CustomJobBuilder` class looks like this:

```groovy
@Override
Job build(Class<? extends Job> jobClass, @DelegatesTo(Job.class) Closure closure) {
    def job = super.build(jobClass, closure)
    job.with {
        if (!DslConfig.get('enableJobs')) {
            disabled()
        }
    }
    return job
}
```

The configuration map is forwarded to the DSL scripts by serializing it to JSON, so only put values in it that can
properly be serialized and deserialized by the `JsonBuilder` and `JsonSlurper` Groovy classes.

Now you can provide a value for the property on the command line to enable or disable your jobs. You can also provide a
default value for the property in your `gradle.properties` file.

```bash
./gradlew -PenableJobs=true dslGenerateXml
```

You can also have server specific configuration in the `build.gradle` file which can be accessed by calling
`DslConfig.get('key')`:

```groovy
jobdsl {
    servers {
        localhost {
            configuration: [ key: 'value' ]
        }
    }
}
```

### Filter Jobs

To update only a part of the configured jobs the filter option can be used. It takes a regular expression as argument
and only job names that match this expression are evaluated:

```bash
# Generate XML only for job names that contain "build"
./gradlew dslGenerateXml --filter=".*build.*"

# Only upload jobs from the folder "MyFolder"
# Note that /.* is put in brackets and made optional with a question mark, this makes sure that also the folder itself is uploaded.
# You could also use "^MyFolder.*", but in this case any item in the folder "MyFolder2" would also be uploaded.
./gradlew dslUpdateJenkins --filter="^MyFolder(/.*)?"
```

## Development

### Build the Plugin

To build the plugin execute:

```bash
# Only build
./gradlew plugin:assemble

# Publish to local maven repository (version will be 1.0)
./gradlew plugin:publishToMavenLocal
```

### Test the Plugin

To run the tests of the plugin execute:

```bash
./gradlew plugin:check
```

## Examples

The repository contains an example project that demonstrates most features of the plugin. To run it you have to deploy
a build of the plugin to your local Maven repository first, as documented in "Build the Plugin" above.

## Known Bugs

- If a folder contains a view it is always updated because the view XML is stored as part of the folder XML on Jenkins,
  but this is not known on the client side at the time the folder is uploaded.
- Views are always updated, because the XML received from Jenkins does not contain the name tag and therefore is
  different to the XML generated by Job DSL.
- If only the order of post build steps is changed this is not detected and the job is not updated.

## Release Notes

### 1.0.32 (upcoming)

- Upgrade job-dsl-core to 1.61
- Server specific configuration now overrides global configuration with the same key

### 1.0.31 (2016-12-16)

- Add support for crumbs for Jenkins with enabled CSRF protection.

### 1.0.30 (2016-11-17)

- Fix a bug when executing DSL tasks on Windows.

### 1.0.29 (2016-08-25)

- Improve performance of the Groovy script used in GroovySeedJobBuilder introduced in 1.0.28

### 1.0.28 (2016-08-18)

- New GroovySeedJobBuilder, can be used to create a seed job that runs in a System Groovy build step instead of using
  the REST API. This has a much better performance because it removes the network overhead.
- Add "HERE Proprietary" license to Maven POM
- JobBuilder2: Provide default empty closure for job type methods
- Publish source and Groovydoc artifacts to Maven repository
- Add some functional tests for the plugin
- Upgrade job-dsl-core from 1.45 to 1.49
    - For changelog see https://github.com/jenkinsci/job-dsl-plugin/wiki#release-notes
- Improve error message when config key is not found in DslConfig

### 1.0.27 (2016-06-07)

- Add option --disablePluginChecks to dslUpdateJenkins task
    - This is necessary because on recent Jenkins releases the Overall/Administer permission is required to fetch the
      plugin list. See [SECURITY-250](https://wiki.jenkins-ci.org/display/SECURITY/Jenkins+Security+Advisory+2016-05-11)

### 1.0.26 (2016-06-03)

- Fix bug with closure being executed in wrong order in JobBuilder2
- Fix NullPointerException in PipelineJobBuilder
- Set pipelineBuilder field in PipelineJobBuilder when adding them to a PipelineBuilder

### 1.0.25 (2016-05-17)

- Fix bug in writing views in folders in dslGenerateXml
- Fix job name generation in JobBuilder2 and PipelineJobBuilder
- Forbid using '/' in job names in JobBuilder2
- Add some convenience methods to add DSL code in JobBuilder2

### 1.0.24 (2016-05-11)

- Add support for managing pipelines
    - New PipelineBuilder class
    - New JobBuilder2 implementation

### 1.0.23 (2016-04-25)

- Upgrade job-dsl-core to 1.45
    - Release notes: https://github.com/jenkinsci/job-dsl-plugin/wiki#release-notes
    - Migration guide: https://github.com/jenkinsci/job-dsl-plugin/wiki/Migration
- Do not require trailing slash at Jenkins URL
- Upload changes in defined order to make sure folders are created before the jobs inside them
    - First upload all folders
    - Then upload all jobs
    - Finally upload all views
- Minor bug fixes

### 1.0.22 (2016-04-14)

- Add support for views inside folders
- Do not require Gradle as a dependency for projects using the plugin

### 1.0.21 (2016-04-11)

- Add dry run option for dslUpdateJenkins task
- Improve logging
    - Remove some verbose logs
    - Print summary after dslUpdateJenkins
- Fail dslUpdateJenkins task when not all items/views could be updated
- Fix bug with processing plugin list from server

### 1.0.20 (2016-03-16)

- Add filter option to tasks to select which jobs are processed
- Do not update jobs when the XML has not changed

### 1.0.19 (2016-03-09)

- Upgrade job-dsl-core to 1.43
- Fix Windows related bugs

### 1.0.17 (2016-02-16)

- Improve log output

### 1.0.15 (2016-02-15)

- Add server specific configuration

### 1.0.14 (2016-02-02)

- Add support to configure multiple servers in the build file
- Add configuration map

### 1.0.13 (2016-02-02)

- Improve error logging
- Rename jenkinsPassword to jenkinsApiToken

### 1.0.12 (2016-02-01)

- Initial release

## License

Copyright (c) 2016 HERE Europe B.V.

See the [LICENSE](LICENSE) file in the root of this project for license details.
