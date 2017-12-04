// Example taken from: https://plugins.jenkins.io/jgiven

freeStyleJob('job') {
    publishers {
        jgivenReports {
            excludeEmptyScenarios() // Since 0.10
            html {
                customCss 'some.css'
                customJs 'some.js'
                title 'My Custom Title'
            }
            results '**/*.json'
        }
    }
}
