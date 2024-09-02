package io.huskit.gradle.plugin.internal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InternalEnvironment {

    public static final String EXTENSION_NAME = "__huskit_internal_environment__";
    boolean isCi;
    boolean isTest;

    public boolean isCi() {
        return isCi;
    }

    public boolean isLocal() {
        return !isCi;
    }

    public boolean isTest() {
        return isTest;
    }
}
