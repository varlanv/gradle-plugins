package io.huskit.common;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OsTest implements UnitTest {

    @Test
    void if_linux__then_not_windows() {
        if (Os.isCurrent(Os.LINUX)) {
            assertThat(Os.isCurrent(Os.WINDOWS)).isFalse();
        } else if (Os.isCurrent(Os.WINDOWS)) {
            assertThat(Os.isCurrent(Os.LINUX)).isFalse();
        }
    }
}
