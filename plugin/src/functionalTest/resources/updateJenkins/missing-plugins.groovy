job('missing-plugins') {
    wrappers {
        timestamps()
    }
    steps {
        gradle('check')
    }
}
