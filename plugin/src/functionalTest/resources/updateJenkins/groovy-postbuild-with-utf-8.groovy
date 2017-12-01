freeStyleJob('job') {
        publishers {
            groovyPostBuild('''\
                def summary = manager.createSummary('completed.gif')
                summary.appendText('✔' /* UTF-8 encoded checkmark */, false, false, false, 'green')
                summary.appendText('✖' /* UTF-8 encoded cross */, false, false, false, 'red')
                '''.stripIndent())
        }
}
