package io.huskit.common.function;

import io.huskit.common.Tuple;
import io.huskit.gradle.commontest.UnitTest;
import lombok.Lombok;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
                .hasMessageContaining("Supplier returned null value");
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

    @Test
    void get__when_throws_exception__then_should_throw_same_exception() {
        var counter = new AtomicInteger();
        var subject = MemoizedSupplier.of(() -> {
            throw new IllegalStateException(counter.incrementAndGet() + "");
        });

        assertThatThrownBy(subject::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("1");
        assertThatThrownBy(subject::get)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("1");
    }

    @Test
    @DisplayName("calling get() multiple times should return the same value")
    void get_when_call_multiple_times_should_return_the_same_value() {
        // Given
        var subject = MemoizedSupplier.of(() -> 1);

        // Expect
        assertThat(subject.get()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("if passed supplier returns null, exception should be thrown")
    void if_passed_supplier_returns_null_exception_should_be_thrown() {
        assertThatThrownBy(() -> MemoizedSupplier.of(() -> null).get())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("if two threads call get() at the same time, computation should be performed only once and the result should be shared")
    void if_two_threads_call_get_at_the_same_time_computation_should_be_performed_only_once() throws Exception {
        var latch = new CountDownLatch(1);
        var counter = new AtomicInteger();
        var subject = MemoizedSupplier.of(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw Lombok.sneakyThrow(e);
            }
            return counter.incrementAndGet();
        });

        var thread1Latch = new CountDownLatch(1);
        var thread1Value = new AtomicReference<Integer>();
        var thread2Latch = new CountDownLatch(1);
        var thread2Value = new AtomicReference<Integer>();
        var executorService = Executors.newFixedThreadPool(2);
        try {
            ThrowingSupplier<Tuple<Future<?>, Future<?>>> compute = () -> {
                var future1 = executorService.submit(new Thread(() -> {
                    thread1Latch.countDown();
                    thread1Value.set(subject.get());
                }));
                thread1Latch.await();
                var future2 = executorService.submit(new Thread(() -> {
                    thread2Latch.countDown();
                    thread2Value.set(subject.get());
                }));
                thread2Latch.await();
                return Tuple.of(future1, future2);
            };
            var result = compute.get();
            latch.countDown();
            result.left().get();
            result.right().get();
        } finally {
            executorService.shutdown();
        }

        assertThat(counter.get())
                .as("counter incremented only once")
                .isEqualTo(1);
        assertThat(subject.get())
                .as("subject.get() called only once")
                .isEqualTo(1);
        assertThat(thread1Value.get())
                .as("first thread got the result")
                .isEqualTo(1);
        assertThat(thread2Value.get())
                .as("second thread got same result")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("calling reset() should clear the memoized value")
    void reset_should_clear_memoized_value() {
        // Given
        var firstRef = new AtomicReference<>();
        var subject = MemoizedSupplier.of(Object::new);
        firstRef.set(subject.get());
        assertThat(subject.isInitialized()).isTrue();
        assertThat(subject.get()).isSameAs(firstRef.get());

        // When
        subject.reset();

        // Then
        assertThat(subject.isInitialized()).isFalse();
    }

    @Test
    @DisplayName("calling reset() if the value is not memoized should do nothing")
    void reset_if_value_not_memoized_should_do_nothing() {
        // Given
        var subject = MemoizedSupplier.of(Object::new);

        // When
        subject.reset();

        // Then
        assertThat(subject.isInitialized()).isFalse();
    }

    @Test
    @DisplayName("calling isInitialized() should return true if the value is memoized")
    void isInitialized_should_return_true_if_value_is_memoized() {
        var subject = MemoizedSupplier.of(Object::new);
        assertThat(subject.isInitialized()).isFalse();
        subject.get();
        assertThat(subject.isInitialized()).isTrue();
    }
}
