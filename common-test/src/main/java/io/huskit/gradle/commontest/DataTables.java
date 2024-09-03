package io.huskit.gradle.commontest;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.provider.Arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Builder(toBuilder = true)
@RequiredArgsConstructor
public class DataTables {

    List<Boolean> isCiList;
    List<Boolean> configurationCacheList;
    List<Boolean> buildCacheList;
    List<String> gradleVersions;

    public DataTables(List<Boolean> isCiList, List<Boolean> configurationCacheList, List<Boolean> buildCacheList) {
        this(isCiList, configurationCacheList, buildCacheList, TestGradleVersions.list());
    }

    public static DataTables getDefault() {
        if (Objects.equals(System.getenv("CI"), "true")) {
            return new DataTables(
                    List.of(true),
                    List.of(true),
                    List.of(true),
                    List.of(TestGradleVersions.current()
                    ));
        } else {
            return new DataTables(
                    List.of(true),
                    List.of(true),
                    List.of(true),
                    TestGradleVersions.list());
        }
    }

    public static Stream<Arguments> defaultAsArgs() {
        return getDefault().get().stream().map(Arguments::of);
    }

    public DataTables isCiAlwaysFalse() {
        return toBuilder().isCiList(List.of(false)).build();
    }

    DataTables configurationCacheAlwaysFalse() {
        return toBuilder().configurationCacheList(List.of(false)).build();
    }

    DataTables configurationCacheAlwaysTrue() {
        return toBuilder().configurationCacheList(List.of(true)).build();
    }

    DataTables isCiAlwaysTrue() {
        return toBuilder().isCiList(List.of(true)).build();
    }

    DataTables buildCacheAlwaysTrue() {
        return toBuilder().buildCacheList(List.of(true)).build();
    }

    DataTables buildCacheAlwaysFalse() {
        return toBuilder().buildCacheList(List.of(false)).build();
    }

    List<DataTable> get() {
        List<DataTable> result = new ArrayList<>();
        gradleVersions.forEach(gradleVersion ->
                isCiList.forEach(isCi ->
                        configurationCacheList.forEach(configurationCache ->
                                buildCacheList.forEach(buildCache ->
                                        result.add(new DataTable(isCi, configurationCache, buildCache, gradleVersion))
                                )
                        )
                )
        );
        return result;
    }
}
