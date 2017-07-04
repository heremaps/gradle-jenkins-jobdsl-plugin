package com.here.gradle.plugins.jobdsl

class GradleJobDslPluginException extends RuntimeException {
    GradleJobDslPluginException() {
        super()
    }

    GradleJobDslPluginException(String message) {
        super(message)
    }

    GradleJobDslPluginException(String message, Throwable cause) {
        super(message, cause)
    }
}
