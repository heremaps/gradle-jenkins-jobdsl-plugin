package com.here.gradle.plugins.jobdsl

class GradleJobDslPluginException extends RuntimeException {
    public GradleJobDslPluginException() {
        super();
    }

    public GradleJobDslPluginException(String message) {
        super(message);
    }

    public GradleJobDslPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
