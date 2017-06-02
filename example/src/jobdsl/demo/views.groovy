listView('TestView') {

    description('Simple test view.')

    jobs {
        name('Demo/SimpleJob')
    }

    columns {
        buildButton()
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
    }

    recurse()
}

listView('Demo/TestViewInFolder') {

    description('Test view inside the Demo folder.')

    jobs {
        name('SimpleJob')
    }

    columns {
        buildButton()
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
    }

    recurse()
}
