package io.huskit.common.function;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StrategyMemoizedSupplierTest implements UnitTest {

    @Test
    void get_when_supplier_throws_exception_should_throw_exception() {
        var counter = new AtomicInteger();
        var subject = MemoizedSupplier.ofStrategy(() -> () -> {
            throw new IllegalStateException(counter.incrementAndGet() + "");
        });

        assertThatThrownBy(subject::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("1");
        assertThatThrownBy(subject::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("2");
    }

    @Test
    void get__when_memoizes_exception__then_should_always_throw_same_exception() {
        var counter = new AtomicInteger();
        var subject = MemoizedSupplier.ofStrategy(() -> {
            if (counter.incrementAndGet() == 1) {
                return () -> {
                    throw new IllegalStateException("1");
                };
            } else {
                return () -> {
                    throw new IllegalStateException("2");
                };
            }
        });

        assertThatThrownBy(subject::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("1");
        assertThatThrownBy(subject::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("1");
    }

    @Test
    void get_when_supplier_returns_null_should_throw_exception() {
        var subject = MemoizedSupplier.ofStrategy(() -> () -> null);

        assertThatThrownBy(subject::get)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Strategy returned null value");
    }

    @Test
    void get_when_supplier_is_null_should_throw_exception() {
        var subject = MemoizedSupplier.ofStrategy(() -> null);

        assertThatThrownBy(subject::get)
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Supplier returned null strategy");
    }

    @Test
    void get_when_supplier_is_null_should_throw_exception2() {
        assertThatThrownBy(() -> MemoizedSupplier.ofStrategy(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void reset_when_called_should_reset_memoization() {
        var counter = new AtomicInteger();
        var subject = MemoizedSupplier.ofStrategy(() -> {
            var val = counter.incrementAndGet();
            return () -> val;
        });

        assertThat(subject.get()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo(1);
        subject.reset();
        assertThat(subject.get()).isEqualTo(2);
        assertThat(subject.get()).isEqualTo(2);
    }

    @Test
    void isInitialized_when_not_called_should_return_false() {
        var subject = MemoizedSupplier.ofStrategy(() -> () -> 1);

        assertThat(subject.isInitialized()).isFalse();
    }

    @Test
    void isInitialized_when_called_should_return_true() {
        var subject = MemoizedSupplier.ofStrategy(() -> () -> 1);

        subject.get();

        assertThat(subject.isInitialized()).isTrue();
    }
}
