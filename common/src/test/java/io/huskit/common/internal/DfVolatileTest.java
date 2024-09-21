package io.huskit.common.internal;

import io.huskit.common.function.ThrowingSupplier;
import io.huskit.gradle.commontest.UnitTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DfVolatileTest implements UnitTest {

    String subjectValue = "value";

    @Test
    void set__when_value_was_null__sets_value() {
        var subject = new DfVolatile<String>();

        subject.set(subjectValue);

        assertThat(subject.require()).isEqualTo(subjectValue);
    }

    @Test
    void set__when_switching_value__sets_new_value() {
        var subject = new DfVolatile<String>();

        subject.set(subjectValue);
        subject.set("new value");

        assertThat(subject.require()).isEqualTo("new value");
    }

    @Test
    void set__when_pass_null__throws_exception() {
        var subject = new DfVolatile<String>();

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

    @Test
    void require__when_value_was_set__returns_value() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.require()).isEqualTo(subjectValue);
    }

    @Test
    void require__when_value_was_not_set__throws_exception() {
        var subject = new DfVolatile<String>();

        assertThatThrownBy(subject::require)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value present");
    }

    @Test
    void ifPresent__when_value_was_set__executes_consumer() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);
        var wasCalled = new AtomicBoolean();

        subject.ifPresent(val -> {
            assertThat(val).isEqualTo(subjectValue);
            wasCalled.set(true);
        });
        assertThat(wasCalled).isTrue();
    }

    @Test
    void maybe__when_value_was_set__returns_optional_with_value() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.maybe()).contains(subjectValue);
    }

    @Test
    void maybe__when_value_was_not_set__returns_empty_optional() {
        var subject = new DfVolatile<String>();

        assertThat(subject.maybe()).isEmpty();
    }

    @Test
    void isEmpty__when_value_was_set__returns_false() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.isEmpty()).isFalse();
    }

    @Test
    void isEmpty__when_value_was_not_set__returns_true() {
        var subject = new DfVolatile<String>();

        assertThat(subject.isEmpty()).isTrue();
    }

    @Test
    void isPresent__when_value_was_set__returns_true() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.isPresent()).isTrue();
    }

    @Test
    void isPresent__when_value_was_not_set__returns_false() {
        var subject = new DfVolatile<String>();

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

    @Test
    void or__when_value_is_present__returns_value() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.or("other")).isEqualTo(subjectValue);
    }

    @Test
    void or__when_value_is_not_present__returns_other() {
        var subject = new DfVolatile<String>();

        assertThat(subject.or("other")).isEqualTo("other");
    }

    @Test
    void or__when_other_is_null__and_value_is_present__returns_value() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.or(null)).isEqualTo(subjectValue);
    }

    @Test
    void or__when_other_is_null__and_value_is_not_present__throws_exception() {
        var subject = new DfVolatile<String>();

        assertThatThrownBy(() -> subject.or(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("other");
    }

    @Test
    void check__when_value_is_present__and_predicate_returns_true__returns_true() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.check(val -> val.equals(subjectValue))).isTrue();
    }

    @Test
    void check__when_value_is_present__and_predicate_returns_false__returns_false() {
        var subject = new DfVolatile<String>();
        subject.set(subjectValue);

        assertThat(subject.check("other"::equals)).isFalse();
    }

    @Test
    void check__when_value_is_not_present__returns_false_and_does_not_call_predicate() {
        var subject = new DfVolatile<String>();

        assertThat(subject.check(val -> {
            throw new IllegalStateException("Should not be called");
        })).isFalse();
    }

    @Test
    void check__when_predicate_throws_exception__should_not_be_ignored() {
        var subject = new DfVolatile<>("value");

        assertThatThrownBy(() ->
                subject.check(val -> {
                    throw new IOException("msg");
                })
        ).isInstanceOf(IOException.class).hasMessage("msg");
    }
}
