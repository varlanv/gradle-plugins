package io.huskit.gradle.commontest

import groovy.transform.CompileStatic

@CompileStatic
class DataTable {

    Boolean isCi
    Boolean configurationCache
    Boolean buildCache
    String gradleVersion

    DataTable(Boolean isCi, Boolean configurationCache, Boolean buildCache, String gradleVersion) {
        this.isCi = isCi
        this.configurationCache = configurationCache
        this.buildCache = buildCache
        this.gradleVersion = gradleVersion
    }
}
