package io.huskit.gradle.common.function;

import io.huskit.gradle.commontest.BaseStatelessUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoizedSupplierTest extends BaseStatelessUnitTest {

    @Test
    @DisplayName("calling get() multiple times should return the same value")
    void test_0() {
        // Given
        var subject = new MemoizedSupplier<>(() -> 1);

        // Expect
        assertThat(subject.get()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("if passed supplier returns null, exception should be thrown")
    void test_1() {
        assertThatThrownBy(() -> new MemoizedSupplier<>(() -> null).get())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("if get() when run in parallel, actual supplier should only be called once")
    void test_2() {
        // Given
        var counter = new AtomicInteger();
        var subject = new MemoizedSupplier<>(counter::incrementAndGet);

        // Expect
        parallel(() -> assertThat(subject.get()).isEqualTo(1));
        assertThat(counter.get()).isEqualTo(1);
    }
}
