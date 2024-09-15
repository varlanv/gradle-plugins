package io.huskit.common;

import io.huskit.common.internal.DfVolatile;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VolatileTest implements UnitTest {

    @Test
    void of__no_args__returns_empty_volatile() {
        var subject = Volatile.of();

        assertThat(subject.isPresent()).isFalse();
        assertThat(subject).isInstanceOf(DfVolatile.class);
    }

    @Test
    void of__with_arg__returns_volatile_with_value() {
        var value = "value";
        var subject = Volatile.of(value);

        assertThat(subject.require()).isEqualTo(value);
        assertThat(subject).isInstanceOf(DfVolatile.class);
    }

    @Test
    void of__another_volatile__returns_volatile_with_value() {
        var value = "value";
        var other = Volatile.of(value);
        var subject = Volatile.of(other);

        assertThat(subject.require()).isEqualTo(value);
        assertThat(subject).isInstanceOf(DfVolatile.class);
    }
}
