package io.huskit.gradle.commontest

import groovy.transform.CompileStatic

@CompileStatic
class DataTables {

    private List<Boolean> isCiList
    private List<Boolean> configurationCacheList
    private List<Boolean> buildCacheList
    private List<String> gradleVersions

    DataTables(List<Boolean> isCiList, List<Boolean> configurationCacheList, List<Boolean> buildCacheList) {
        this.isCiList = isCiList
        this.configurationCacheList = configurationCacheList
        this.buildCacheList = buildCacheList
        this.gradleVersions = TestGradleVersions.get()
    }

    static DataTables getDefault() {
        return new DataTables(
                [true, false],
                [true, false],
                [true, false]
        )
    }

    DataTables isCiAlwaysFalse() {
        isCiList = [false, ]
        return this
    }

    DataTables configurationCacheAlwaysFalse() {
        configurationCacheList = [false, ]
        return this
    }

    DataTables configurationCacheAlwaysTrue() {
        configurationCacheList = [true, ]
        return this
    }

    DataTables isCiAlwaysTrue() {
        isCiList = [true, ]
        return this
    }

    DataTables buildCacheAlwaysTrue() {
        buildCacheList = [true, ]
        return this
    }

    DataTables buildCacheAlwaysFalse() {
        buildCacheList = [false, ]
        return this
    }

    List<DataTable> get() {
        List<DataTable> result = new ArrayList<>()
        gradleVersions.each { gradleVersion ->
            isCiList.each { isCi ->
                configurationCacheList.each { configurationCache ->
                    buildCacheList.each { buildCache ->
                        result.add(new DataTable(isCi, configurationCache, buildCache, gradleVersion))
                    }
                }
            }
        }
        return result
    }
}
