package io.huskit.common;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SneakyTest implements UnitTest {

    @Test
    void rethrow__when_throwable__throws_throwable() {
        var throwable = new Throwable();

        assertThatThrownBy(() -> Sneaky.rethrow(throwable))
                .isSameAs(throwable);
    }

    @Test
    void rethrow__when_null__throws_null_pointer_exception() {
        assertThatThrownBy(() -> Sneaky.rethrow(null))
                .isInstanceOf(NullPointerException.class);
    }
}
