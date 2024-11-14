package io.huskit.gradle.commontest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public final class DataTable {

    boolean isCi;
    boolean configurationCache;
    Boolean buildCache;
    String gradleVersion;
}
