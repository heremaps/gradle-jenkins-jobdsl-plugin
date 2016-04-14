package com.here.gradle.plugins.jobdsl

import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import spock.lang.Specification

class RestJobManagementSpec extends Specification {

    RestJobManagement jobManagement = new RestJobManagement(null, false, 'http://localhost:8080', null, null)

    def 'trailing slash is added to jenkins URL'() {
        setup:
        jobManagement = new RestJobManagement(null, false, 'http://localhost:8080', null, null)

        expect:
        jobManagement.getJenkinsUrl() == 'http://localhost:8080/'
    }

    def 'config path for non-folder item is generated correctly'() {
        setup:
        Item item = new FreeStyleJob(jobManagement)
        item.setName('FreeStyleJob')

        expect:
        jobManagement.getItemConfigPath(item) == 'job/FreeStyleJob/config.xml'
    }

    def 'config path for item in folder is generated correctly'() {
        setup:
        Item item = new FreeStyleJob(jobManagement)
        item.setName('Folder/FreeStyleJob')

        expect:
        jobManagement.getItemConfigPath(item) == 'job/Folder/job/FreeStyleJob/config.xml'
    }

    def 'config path for item in nested folders is generated correctly'() {
        setup:
        Item item = new FreeStyleJob(jobManagement)
        item.setName('Folder1/Folder2/Folder3/FreeStyleJob')

        expect:
        jobManagement.getItemConfigPath(item) == 'job/Folder1/job/Folder2/job/Folder3/job/FreeStyleJob/config.xml'
    }

    def 'config path for non-folder view is generated correctly'() {
        expect:
        jobManagement.getViewConfigPath('View') == 'view/View/config.xml'
    }

    def 'config path for view in folder is generated correctly'() {
        expect:
        jobManagement.getViewConfigPath('Folder/View') == 'job/Folder/view/View/config.xml'
    }

    def 'config path for view in nested folders is generated correctly'() {
        expect:
        jobManagement.getViewConfigPath('Folder1/Folder2/Folder3/View') ==
                'job/Folder1/job/Folder2/job/Folder3/view/View/config.xml'
    }

    def 'create path for non-folder name is generated correctly'() {
        expect:
        jobManagement.getCreatePath('Item', 'method') == 'method'
    }

    def 'create path for name in folder is generated correctly'() {
        expect:
        jobManagement.getCreatePath('Folder/Item', 'method') == 'job/Folder/method'
    }

    def 'create path for name nested folders is generated correctly'() {
        expect:
        jobManagement.getCreatePath('Folder1/Folder2/Folder3/Item', 'method') ==
                'job/Folder1/job/Folder2/job/Folder3/method'
    }

}
