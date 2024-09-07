package io.huskit.containers.model;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExistingContainerTest implements UnitTest {

    @Test
    @DisplayName("`isExpired` if container is expired, should return true")
    void test_0() {
        var nowMinus10Sec = System.currentTimeMillis() - Duration.ofSeconds(10).toMillis();
        var subject = buildSubject(nowMinus10Sec);

        var actual = subject.isExpired(Duration.ofSeconds(5));

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("`isExpired` if container not expired, should return false")
    void test_1() {
        var nowMinus10Sec = System.currentTimeMillis() - Duration.ofSeconds(10).toMillis();
        var subject = buildSubject(nowMinus10Sec);

        var actual = subject.isExpired(Duration.ofSeconds(15));

        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("`isExpired` if cleanupAfter is 0, should return false")
    void test_2() {
        var subject = buildSubject(System.currentTimeMillis());

        assertThat(subject.isExpired(Duration.ofSeconds(0))).isFalse();
    }

    private ExistingContainer buildSubject(long createdAt) {
        return new ExistingContainer("id", "containerId", createdAt, Map.of());
    }
}
