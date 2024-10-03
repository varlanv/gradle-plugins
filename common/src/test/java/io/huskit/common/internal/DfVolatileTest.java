package io.huskit.common.internal;

import io.huskit.common.Mutable;
import io.huskit.common.function.ThrowingSupplier;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DfVolatileTest implements UnitTest {

    String subjectValue = "value";

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void set__when_value_was_null__sets_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        subject.set(subjectValue);

        assertThat(subject.require()).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void set__when_switching_value__sets_new_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        subject.set(subjectValue);
        subject.set("new value");

        assertThat(subject.require()).isEqualTo("new value");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void set__when_pass_null__throws_exception(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThatThrownBy(() -> subject.set(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(subjectValue);
    }

    @Test
    void reset__when_value_was_set__resets_value() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        subject.reset();

        assertThat(subject.isPresent()).isFalse();
    }

    @Test
    void reset__when_value_was_not_set__does_nothing() {
        var subject = new DfVolatile<String>();

        subject.reset();

        assertThat(subject.get()).isNull();
    }

    @Test
    void get__when_value_was_set__returns_value() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.get()).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void require__when_value_was_set__returns_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.require()).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void require__when_value_was_not_set__throws_exception(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThatThrownBy(subject::require)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void ifPresent__when_value_was_set__executes_consumer(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);
        var wasCalled = new AtomicBoolean();

        subject.ifPresent(val -> {
            assertThat(val).isEqualTo(subjectValue);
            wasCalled.set(true);
        });
        assertThat(wasCalled).isTrue();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void maybe__when_value_was_set__returns_optional_with_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.maybe()).contains(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void maybe__when_value_was_not_set__returns_empty_optional(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThat(subject.maybe()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void isEmpty__when_value_was_set__returns_false(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.isEmpty()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void isEmpty__when_value_was_not_set__returns_true(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThat(subject.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void isPresent__when_value_was_set__returns_true(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.isPresent()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void isPresent__when_value_was_not_set__returns_false(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThat(subject.isPresent()).isFalse();
    }

    @Test
    void if_two_threads_call_syncSetOrGet_at_the_same_time_computation_should_be_performed_only_once() throws Exception {
        var subject = new DfVolatile<String>();
        var counter = new AtomicInteger();
        var threadsReadyLatch = new CountDownLatch(2);
        var caseReadyLatch = new CountDownLatch(1);
        var threadsFinishedLatch = new CountDownLatch(2);
        var valueSupplier = new ThrowingSupplier<String>() {
            @Override
            @SneakyThrows
            public String get() {
                counter.incrementAndGet();
                return subjectValue;
            }
        };
        Runnable runnable = () -> {
            threadsReadyLatch.countDown();
            try {
                caseReadyLatch.await();
            } catch (InterruptedException e) {
                throw hide(e);
            }
            subject.syncSetOrGet(valueSupplier);
            threadsFinishedLatch.countDown();
        };
        new Thread(runnable).start();
        new Thread(runnable).start();
        threadsReadyLatch.await();
        caseReadyLatch.countDown();
        threadsFinishedLatch.await();

        assertThat(counter.get()).isEqualTo(1);
        assertThat(subject.get()).isEqualTo(subjectValue);
    }

    @Test
    void syncSetOrGet__if_value_is_already_set__returns_value() throws Exception {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        var result = subject.syncSetOrGet(() -> {
            throw new IllegalStateException("Should not be called");
        });

        assertThat(result).isEqualTo(subjectValue);
    }

    @Test
    void syncSetOrGet__if_supplier_throws_exception__should_not_be_ignored() {
        var subject = new DfVolatile<String>();
        var exception = new IllegalStateException("msg");

        assertThatThrownBy(() ->
                subject.syncSetOrGet(() -> {
                    throw exception;
                })
        ).isSameAs(exception);
    }

    @Test
    void when__constructing_from_another_volatile__copies_value() {
        var another = new DfVolatile<String>();
        another.set(subjectValue);

        var subject = new DfVolatile<>(another);

        assertThat(subject.get()).isEqualTo(subjectValue);
    }

    @Test
    void when__constructing_from_empty_volatile__copies_nothing() {
        var another = new DfVolatile<String>();

        var subject = new DfVolatile<>(another);

        assertThat(subject.isEmpty()).isTrue();
    }

    @Test
    void when__constructing_from_value__use_value() {
        var subject = new DfVolatile<>(subjectValue);

        assertThat(subject.get()).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_value_is_present__returns_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.or("other")).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_value_is_not_present__returns_other(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThat(subject.or("other")).isEqualTo("other");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_other_is_null__and_value_is_present__returns_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.or((String) null)).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_other_is_null__and_value_is_not_present__throws_exception(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThatThrownBy(() -> subject.or((String) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("other");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_supplier_is_present__returns_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.or(() -> "other")).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_supplier_is_not_present__returns_other(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThat(subject.or(() -> "other")).isEqualTo("other");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_supplier_is_null__and_value_is_present__returns_value(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.or((ThrowingSupplier<String>) null)).isEqualTo(subjectValue);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_supplier_is_null__and_value_is_not_present__throws_exception(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThatThrownBy(() -> subject.or((ThrowingSupplier<String>) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(null);
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_supplier_throws_exception__should_not_be_ignored(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThatThrownBy(() -> subject.or(() -> {
            throw new IOException("msg");
        })).isInstanceOf(IOException.class).hasMessage("msg");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void or__when_supplier_returns_null__throws_exception(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThatThrownBy(() -> subject.or(() -> null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("supplier");
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void check__when_value_is_present__and_predicate_returns_true__returns_true(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.check(val -> val.equals(subjectValue))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void check__when_value_is_present__and_predicate_returns_false__returns_false(Supplier<Mutable<String>> factory) {
        var subject = factory.get();
        subject.set(subjectValue);

        assertThat(subject.check("other"::equals)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("emptyMutableFactories")
    void check__when_value_is_not_present__returns_false_and_does_not_call_predicate(Supplier<Mutable<String>> factory) {
        var subject = factory.get();

        assertThat(subject.check(val -> {
            throw new IllegalStateException("Should not be called");
        })).isFalse();
    }

    @ParameterizedTest
    @MethodSource("mutableFactories")
    void check__when_predicate_throws_exception__should_not_be_ignored(Function<String, Mutable<String>> factory) {
        var subject = factory.apply("value");

        assertThatThrownBy(() ->
                subject.check(val -> {
                    throw new IOException("msg");
                })
        ).isInstanceOf(IOException.class).hasMessage("msg");
    }

    Stream<Arguments> mutableFactories() {
        return Stream.of(
                Arguments.of((Function<String, Mutable<String>>) DfVolatile::new),
                Arguments.of((Function<String, Mutable<String>>) DfMutable::new)
        );
    }

    Stream<Arguments> emptyMutableFactories() {
        return Stream.of(
                Arguments.of((Supplier<Mutable<String>>) DfVolatile::new),
                Arguments.of((Supplier<Mutable<String>>) DfMutable::new)
        );
    }
}
