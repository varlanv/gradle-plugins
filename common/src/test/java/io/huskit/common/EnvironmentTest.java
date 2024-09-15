package io.huskit.common;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnvironmentTest implements UnitTest {

    @Test
    void if_linux__then_not_windows() {
        if (Environment.is(Environment.LINUX)) {
            assertThat(Environment.is(Environment.WINDOWS)).isFalse();
        } else if (Environment.is(Environment.WINDOWS)) {
            assertThat(Environment.is(Environment.LINUX)).isFalse();
        }
    }
}
