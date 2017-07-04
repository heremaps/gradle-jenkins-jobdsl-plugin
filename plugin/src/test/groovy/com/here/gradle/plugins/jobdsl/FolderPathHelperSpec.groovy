package com.here.gradle.plugins.jobdsl

import javaposse.jobdsl.dsl.Item
import javaposse.jobdsl.dsl.JobManagement
import javaposse.jobdsl.dsl.MemoryJobManagement
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import spock.lang.Specification

class FolderPathHelperSpec extends Specification {

    JobManagement jobManagement = new MemoryJobManagement()

    def 'config path for non-folder item is generated correctly'() {
        setup:
        Item item = new FreeStyleJob(jobManagement)
        item.setName('FreeStyleJob')

        expect:
        FolderPathHelper.itemConfigPath(item.name) == 'job/FreeStyleJob/config.xml'
    }

    def 'config path for item in folder is generated correctly'() {
        setup:
        Item item = new FreeStyleJob(jobManagement)
        item.setName('Folder/FreeStyleJob')

        expect:
        FolderPathHelper.itemConfigPath(item.name) == 'job/Folder/job/FreeStyleJob/config.xml'
    }

    def 'config path for item in nested folders is generated correctly'() {
        setup:
        Item item = new FreeStyleJob(jobManagement)
        item.setName('Folder1/Folder2/Folder3/FreeStyleJob')

        expect:
        FolderPathHelper.itemConfigPath(item.name) == 'job/Folder1/job/Folder2/job/Folder3/job/FreeStyleJob/config.xml'
    }

    def 'config path for non-folder view is generated correctly'() {
        expect:
        FolderPathHelper.viewConfigPath('View') == 'view/View/config.xml'
    }

    def 'config path for view in folder is generated correctly'() {
        expect:
        FolderPathHelper.viewConfigPath('Folder/View') == 'job/Folder/view/View/config.xml'
    }

    def 'config path for view in nested folders is generated correctly'() {
        expect:
        FolderPathHelper.viewConfigPath('Folder1/Folder2/Folder3/View') ==
                'job/Folder1/job/Folder2/job/Folder3/view/View/config.xml'
    }

    def 'REST method path for non-folder name is generated correctly'() {
        expect:
        FolderPathHelper.restMethodPathForName('Item', 'method') == 'method'
    }

    def 'REST method path for name in folder is generated correctly'() {
        expect:
        FolderPathHelper.restMethodPathForName('Folder/Item', 'method') == 'job/Folder/method'
    }

    def 'REST method path for name nested folders is generated correctly'() {
        expect:
        FolderPathHelper.restMethodPathForName('Folder1/Folder2/Folder3/Item', 'method') ==
                'job/Folder1/job/Folder2/job/Folder3/method'
    }

}
