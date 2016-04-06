package com.here.gradle.plugins.jobdsl

import org.gradle.api.GradleException

class GradleJobDslPluginException extends GradleException {
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
