# Gradle Jenkins Job DSL Plugin

| Linux | Windows |
| :---- | :------ |
[ ![Linux build status][1]][2] | [![Windows build status][3]][4] |

[1]: https://travis-ci.com/heremaps/gradle-jenkins-jobdsl-plugin.svg?branch=master
[2]: https://travis-ci.com/heremaps/gradle-jenkins-jobdsl-plugin
[3]: https://ci.appveyor.com/api/projects/status/37g184qui7ityfw2/branch/master?svg=true
[4]: https://ci.appveyor.com/project/heremaps/gradle-jenkins-jobdsl-plugin/branch/master

This is a plugin to manage Jenkins [Job DSL](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin) projects in a
Gradle project.

## Contents

- [Usage](#usage)
    - [Apply Plugin](#apply-plugin)
    - [How does it work](#how-does-it-work)
    - [Writing Job DSL Scripts](#writing-job-dsl-scripts)
        - [Helper Classes](#helper-classes)
        - [Job Builders](#job-builders)
        - [Thirdparty Libraries](#thirdparty-libraries)
    - [Generate XML](#generate-xml)
    - [Upload Jobs to Jenkins](#upload-jobs-to-jenkins)
    - [Configure Servers](#configure-servers)
    - [Seed Job](#seed-job)
    - [Custom Configuration](#custom-configuration)
    - [Filter Jobs](#filter-jobs)
    - [Generated DSL and Job DSL extensions](#generated-dsl-and-job-dsl-extensions)
- [Development](#development)
    - [Build the Plugin](#build-the-plugin)
    - [Test the Plugin](#test-the-plugin)
    - [Debugging the DSL tasks](#debugging-the-dsl-tasks)
    - [Contributing](#contributing)
- [Examples](#examples)
- [Hints](#hints)
- [Credits](#credits)
- [License](#license)

## Usage

The plugin can be used to locally generate XML files for the jobs and to upload the generated jobs to a Jenkins
instance.

### Apply Plugin

To apply the plugin you need to add the necessary Maven repositories to the build script. The minimal build script looks
like this:

```groovy
buildscript {
    ext.jenkinsJobdslPluginVersion = '3.6.0'

    repositories {
        maven {
            url 'https://plugins.gradle.org/m2'
        }

        maven {
            url 'https://repo.jenkins-ci.org/releases/'
        }
    }

    dependencies {
        classpath("com.here.gradle.plugins:gradle-jenkins-jobdsl-plugin:${jenkinsJobdslPluginVersion}") {
            // Exclude unneeded transitive dependencies that can cause dependency resolution issues.
            exclude(module: 'bootstrap')
            exclude(module: 'groovy-all')
            exclude(module: 'org-netbeans-insane')

            // Exclude unneeded transitive dependencies on XML libraries that can break the generation of config files.
            // See: https://issues.jenkins-ci.org/browse/JENKINS-35638
            exclude(module: 'xalan')
            exclude(module: 'xercesImpl')
        }
    }
}

apply plugin: 'com.here.jobdsl'

repositories {
    maven {
        url 'https://plugins.gradle.org/m2'
    }
}

dependencies {
    compile 'org.codehaus.groovy:groovy-all:2.4.15'

    compile("com.here.gradle.plugins:gradle-jenkins-jobdsl-plugin:${jenkinsJobdslPluginVersion}") {
        exclude(module: 'org-netbeans-insane')
        exclude(module: 'xalan')
        exclude(module: 'xercesImpl')
    }
}
```

### How does it work

To come as close as possible to the behaviour of a normal Job DSL
[seed job](https://github.com/jenkinsci/job-dsl-plugin/wiki/Tutorial---Using-the-Jenkins-Job-DSL#1-creating-the-seed-job)
the plugin launches a Jenkins instance in the background and executes the Job DSL scripts inside of this Jenkins. This
implementation enables support for the generated DSL and Job DSL extensions as described below.

To load plugins in this Jenkins instance they have to be added as dependencies to the `jenkinsPlugins` configuration:

```groovy
dependencies {
    // Some Job DSL features only work when plugins are installed, for example cloudbees-folder is required
    // to be able to create folders (https://jenkinsci.github.io/job-dsl-plugin/#path/folder).
    jenkinsPlugins 'org.jenkins-ci.plugins:cloudbees-folder:6.2.1'
}
```

Known issues with plugins:

* The `org.jenkins-ci.plugins:ws-cleanup` plugin has a dependency on an old version of `jenkins-core`. This adds some
  XML parser libraries to the classpath which conflict with the XML parser expected by newer Jenkins versions. As
  `jenkins-core` is provided anyway it should be excluded:

```groovy
jenkinsPlugins('org.jenkins-ci.plugins:ws-cleanup:0.34') {
    exclude module: 'jenkins-core'
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
enables the timestamp plugin for all jobs that use the template:

```groovy
import com.here.gradle.plugins.jobdsl.util.JobBuilder
import javaposse.jobdsl.dsl.Job

class CustomJobBuilder extends JobBuilder {

    CustomJobBuilder(DslFactory dslFactory) {
        super(dslFactory)

        addDsl {
            wrappers {
                timestamps()
            }
        }
    }

}
```

To use the template you have to create an instance of your custom job builder class. You can add additional DSL code by
passing it to the `addDsl` method. The builder will concatenate all closures and execute them in the order they were
added when `build` is called. The `JobBuilder` provides convenience methods like `addFreeStyleDsl` or `addMatrixdsl` for
each job type that optimize IDE support compared to using the generic `addDsl` method. You also have to set the job type
by calling any of `freestyleJob`, `matrixJob` and so on. These methods optionally take a DSL closure argument for
convenience.

```groovy
def job = new CustomJobBuilder(
        dslFactory: this,
        name: 'MyExampleJob'
).freeStyleJob {

    description 'My example job'

    steps {
        shell('Hello World!')
    }
}

job.build()
```

#### Thirdparty Libraries

To use thirdparty libraries in your Job DSL scripts add them to the dependencies closure of the build script. For
example the Job DSL plugin itself is added there to be able to use the `JobBuilder` class. This example shows how to add
the Google Guava library:

```groovy
dependencies {
    compile localGroovy()
    compile "com.here.gradle.plugins:gradle-jenkins-jobdsl-plugin:${jobDslPluginVersion}"
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
        disableJobs: project.properties['disableJobs']?.toBoolean() ?: false
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
        disabled(DslConfig.get('disableJobs'))
    }
    return job
}
```

The configuration map is forwarded to the DSL scripts by serializing it to JSON, so only put values in it that can
properly be serialized and deserialized by the `JsonBuilder` and `JsonSlurper` Groovy classes.

Now you can provide a value for the property on the command line to enable or disable your jobs. You can also provide a
default value for the property in your `gradle.properties` file.

```bash
./gradlew -PdisableJobs=true dslGenerateXml
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
# Note that /.* is put in brackets and made optional with a question mark, this makes sure that also the folder itself
# is uploaded. You could also use "^MyFolder.*", but in this case any item in the folder "MyFolder2" would also be
# uploaded.
./gradlew dslUpdateJenkins --filter="^MyFolder(/.*)?"
```

### Generated DSL and Job DSL extensions

Job DSL has built-in support for many Jenkins plugins. Additionally there are two mechanisms that add support for
plugins which are not directly supported:

1. [Generated DSL](https://github.com/jenkinsci/job-dsl-plugin/wiki/Automatically-Generated-DSL):
   This method uses annotations to discover configuration endpoints of plugins automatically and generates a DSL for
   them. This works for many plugins when they use the correct annotations.
1. [Job DSL Extensions](https://github.com/jenkinsci/job-dsl-plugin/wiki/Extending-the-DSL):
   Plugins can also define their own DSL which is then available when the plugin is loaded by Jenkins.

Both mechanisms are supported by the gradle-jenkins-jobdsl-plugin. To use them the plugins have to be added to the
`jenkinsPlugins` configuration:

```groovy
dependencies {
    // This plugin defines a Job DSL extension.
    jenkinsPlugins 'org.jenkins-ci.plugins:jgiven:0.15.1'

    // This plugin is supported by the generated DSL.
    jenkinsPlugins 'org.jenkins-ci.plugins:cvs:2.13'
}
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

### Debugging the DSL tasks

To debug the tasks `DslGenerateXml` and `DslUpdateJenkins` using IntelliJ follow these steps:

1. Uncomment these two lines in [`AbstractDslTask`](https://github.com/heremaps/gradle-jenkins-jobdsl-plugin/blob/master/plugin/src/main/groovy/com/here/gradle/plugins/jobdsl/tasks/AbstractDslTask.groovy):
   ```groovy
   //'-Xdebug',
   //'-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005'
   ```
2. Create a test in `GenerateXmlTest.groovy` or `UpdateJenkinsTest.groovy` with the scenario you want to test (e.g.
   usage of specific plugins).
3. Set a breakpoint in the `run` method of `AbstractTaskRunner.groovy`.
4. Create a remote debug configuration in IntelliJ: `Run` -> `Edit Configurations...` -> `+ Remote`
5. Run the test on the command line:
   `./gradlew plugin:functionalTest --tests "com.here.gradle.plugins.jobdsl.tasks.GenerateXmlTest.name of your test"`
6. Start the remote debug configuration in IntelliJ.

### Contributing

Contributions to the project are very welcome! Please be aware that you need to sign the
[HERE CLA](https://cla-assistant.io/heremaps/gradle-jenkins-jobdsl-plugin) if you want to contribute to the project.

## Examples

The repository contains an example project that demonstrates most features of the plugin. To run it you have to deploy
a build of the plugin to your local Maven repository first, as documented in "Build the Plugin" above.

## Hints

### Job DSL Plugin is not required

Because the gradle-jenkins-jobdsl-plugin can use the
[Jenkins REST API](https://wiki.jenkins.io/display/JENKINS/Remote+access+API) to upload jobs to Jenkins the
[Job DSL Plugin](https://wiki.jenkins.io/display/JENKINS/Job+DSL+Plugin) does not have to be installed on the Jenkins
instance. This can be useful when you do not have permission to install Jenkins plugins or the plugin cannot be
installed for another reason.
 
### JobConfigHistory Plugin and seed jobs

If you use the [JobConfigHistory Plugin](https://wiki.jenkins.io/display/JENKINS/JobConfigHistory+Plugin) in combination
with a seed job you should configure retention for history entries in "Manage Jenkins" -> "Configure System". Otherwise
you can quickly get a huge number of history entries which has a very negative impact on seed job performance and
Jenkins performance in general.

## Credits

The plugin was inspired by the following two projects, many thanks to their creators!

- [gradle-jenkins-plugin](https://github.com/ghale/gradle-jenkins-plugin)
- [job-dsl-gradle-example](https://github.com/sheehan/job-dsl-gradle-example)

## License

Copyright (c) 2016-2017 HERE Europe B.V.

See the [LICENSE](LICENSE) file in the root of this project for license details.
