package io.huskit.gradle.commontest

import groovy.transform.CompileStatic

@CompileStatic
class TestGradleVersions {

    static List<String> get() {
        return [
                "8.3",
                "7.6.1",
//                "6.9.4", todo
        ]
    }
}
