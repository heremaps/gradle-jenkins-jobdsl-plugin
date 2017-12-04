// Example taken from: https://github.com/jenkinsci/job-dsl-plugin/wiki/Automatically-Generated-DSL

freeStyleJob('job') {
    scm {
        cvsscm {
            repositories {
                cvsRepository {
                    cvsRoot(':pserver:username@hostname:/opt/path/to/a/repo')
                    passwordRequired(false)
                    password(null)
                    compressionLevel(-1)
                    repositoryBrowser {}
                    repositoryItems {
                        cvsRepositoryItem {
                            modules {
                                cvsModule {
                                    localName('bar')
                                    projectsetFileName('bar')
                                    remoteName('foo')
                                }
                            }
                            location {
                                tagRepositoryLocation {
                                    tagName('test')
                                    useHeadIfNotFound(false)
                                }
                            }
                        }
                    }
                }
            }
            checkoutCurrentTimestamp(true)
            canUseUpdate(true)
            pruneEmptyDirectories(true)
            legacy(false)
            skipChangeLog(false)
            disableCvsQuiet(false)
            cleanOnFailedUpdate(false)
            forceCleanCopy(false)
        }
    }
}
