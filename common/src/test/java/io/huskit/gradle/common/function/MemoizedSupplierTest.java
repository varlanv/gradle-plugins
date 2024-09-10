package io.huskit.gradle.common.function;

import io.huskit.common.Tuple;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.function.ThrowingSupplier;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemoizedSupplierTest implements UnitTest {

    @Test
    @DisplayName("calling get() multiple times should return the same value")
    void get_when_call_multiple_times_should_return_the_same_value() {
        // Given
        var subject = new MemoizedSupplier<>(() -> 1);

        // Expect
        assertThat(subject.get()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("if passed supplier returns null, exception should be thrown")
    void if_passed_supplier_returns_null_exception_should_be_thrown() {
        assertThatThrownBy(() -> new MemoizedSupplier<>(() -> null).get())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("if two threads call get() at the same time, computation should be performed only once and the result should be shared")
    void if_two_threads_call_get_at_the_same_time_computation_should_be_performed_only_once() throws Exception {
        var latch = new CountDownLatch(1);
        var counter = new AtomicInteger();
        var subject = new MemoizedSupplier<>(() -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
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
                return new Tuple<>(future1, future2);
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

    private static class Result {
        public final Future<?> future1;
        public final Future<?> future2;

        public Result(Future<?> future1, Future<?> future2) {
            this.future1 = future1;
            this.future2 = future2;
        }
    }

    @Test
    @DisplayName("calling reset() should clear the memoized value")
    void test_3() {
        // Given
        var firstRef = new AtomicReference<>();
        var subject = new MemoizedSupplier<>(Object::new);
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
    void test_4() {
        // Given
        var subject = new MemoizedSupplier<>(Object::new);

        // When
        subject.reset();

        // Then
        assertThat(subject.isInitialized()).isFalse();
    }

    @Test
    @DisplayName("calling isInitialized() should return true if the value is memoized")
    void test_5() {
        var subject = new MemoizedSupplier<>(Object::new);
        assertThat(subject.isInitialized()).isFalse();
        subject.get();
        assertThat(subject.isInitialized()).isTrue();
    }
}
