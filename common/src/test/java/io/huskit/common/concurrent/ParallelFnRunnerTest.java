package io.huskit.common.concurrent;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParallelFnRunnerTest implements UnitTest {

    @Test
    void doParallel__when_empty__returns_empty() {
        var subject = new ParallelFnRunner<String, String>(List.of());
        Function<String, String> function = a -> {
            throw new RuntimeException("Should not be called");
        };

        var result = subject.doParallel(function);

        assertThat(result).isEmpty();
    }

    @Test
    void doParallel__when_one_element__should_run_in_same_thread() {
        var subject = new ParallelFnRunner<String, String>(List.of(() -> "value"));
        var fnThread = new AtomicReference<Thread>();
        Function<String, String> function = a -> {
            fnThread.set(Thread.currentThread());
            return a;
        };

        var result = subject.doParallel(function);

        assertThat(result).containsExactly("value");
        assertThat(fnThread.get()).isEqualTo(Thread.currentThread());
    }

    @RepeatedTest(5)
    void doParallel__when_two_element__should_return_ordered_results() {
        var subject = new ParallelFnRunner<String, String>(
                List.of(
                        () -> "value1",
                        () -> "value2"
                ));
        Function<String, String> function = a -> a;

        var result = subject.doParallel(function);

        assertThat(result).containsExactly("value1", "value2");
    }

    @Test
    void doParallel__two_elements_with_consumer__should_return_ordered_results() {
        var subject = new ParallelFnRunner<String, String>(
                List.of(
                        () -> "value1",
                        () -> "value2"
                ));
        List<String> result = new CopyOnWriteArrayList<>();
        Consumer<String> consumer = result::add;

        subject.doParallel(consumer);

        assertThat(result).containsExactlyInAnyOrder("value1", "value2");
    }

    @Test
    void doParallel__when_exception__should_throw_execution_exception() {
        var exception = new RuntimeException("bad");
        var secondResult = new AtomicReference<>();
        var subject = new ParallelFnRunner<String, String>(
                List.of(
                        () -> {
                            throw exception;
                        },
                        () -> {
                            secondResult.set("value2");
                            return "value2";
                        }
                )
        );

        assertThatThrownBy(() -> subject.doParallel(Function.identity()))
                .isInstanceOf(ExecutionException.class)
                .hasCause(exception);
        assertThat(secondResult.get()).isEqualTo("value2");
    }
}
