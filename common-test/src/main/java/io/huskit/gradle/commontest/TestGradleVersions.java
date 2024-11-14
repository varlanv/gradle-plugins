package io.huskit.gradle.commontest;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public final class TestGradleVersions {

    List<String> list() {
        return List.of(
            current()
//                latest7(),
//                latest6()
        );
    }

    String current() {
        return latest8();
    }

    String latest7() {
        return "7.6.1";
    }

    String latest8() {
        return "8.10.2";
    }

    String latest6() {
        return "6.9.4";
    }
}
