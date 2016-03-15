package com.here.gradle.plugins.jobdsl.tasks.runners

import com.here.gradle.plugins.jobdsl.ItemFilter
import com.here.gradle.plugins.jobdsl.RestJobManagement
import javaposse.jobdsl.dsl.JobManagement

class UpdateJenkinsRunner extends AbstractTaskRunner {

    public static void main(String[] args) {
        new UpdateJenkinsRunner().run()
    }

    @Override
    JobManagement createJobManagement(ItemFilter filter) {
        String jenkinsUrl = runProperties['jenkinsUrl']
        String jenkinsUser = runProperties['jenkinsUser']
        String jenkinsApiToken = runProperties['jenkinsApiToken']

        new RestJobManagement(filter, jenkinsUrl, jenkinsUser, jenkinsApiToken)
    }
}
