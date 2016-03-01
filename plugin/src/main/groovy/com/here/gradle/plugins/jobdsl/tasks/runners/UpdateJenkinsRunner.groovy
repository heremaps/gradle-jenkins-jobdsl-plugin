package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.RestJobManagement
import javaposse.jobdsl.dsl.JobManagement

class UpdateJenkinsRunner extends AbstractTaskRunner {

    public static void main(String[] args) {
        new UpdateJenkinsRunner().run()
    }

    @Override
    JobManagement createJobManagement() {
        String jenkinsUrl = runProperties['jenkinsUrl']
        String jenkinsUser = runProperties['jenkinsUser']
        String jenkinsApiToken = runProperties['jenkinsApiToken']

        new RestJobManagement(jenkinsUrl, jenkinsUser, jenkinsApiToken)
    }
}
