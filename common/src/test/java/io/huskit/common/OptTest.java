package io.huskit.common;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OptTest implements UnitTest {

    String value = "value";

    @Nested
    class SomeTest {

        @Test
        void of__returns_some_instance() {
            assertThat(Opt.of(value)).isInstanceOf(Some.class);
        }

        @Test
        void of__throws_exception_when_value_is_null() {
            assertThatThrownBy(() -> Opt.of(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void require__returns_value() {
            assertThat(new Some<>(value).require()).isEqualTo(value);
        }

        @Test
        void isPresent__returns_true() {
            assertThat(new Some<>(value).isPresent()).isTrue();
        }

        @Test
        void isEmpty__returns_false() {
            assertThat(new Some<>(value).isEmpty()).isFalse();
        }

        @Test
        void ifPresent__executes_consumer() {
            var subject = new Some<>(value);
            var result = new StringBuilder();

            subject.ifPresent(result::append);

            assertThat(result.toString()).isEqualTo(value);
        }
    }

    @Nested
    class NoneTest {

        @Test
        void require__throws_exception() {
            assertThatThrownBy(() -> None.instance().require())
                    .isInstanceOf(NoSuchElementException.class);
        }

        @Test
        void isPresent__returns_false() {
            assertThat(None.instance().isPresent()).isFalse();
        }

        @Test
        void isEmpty__returns_true() {
            assertThat(None.instance().isEmpty()).isTrue();
        }

        @Test
        void ifPresent__does_nothing() {
            var result = new StringBuilder();

            None.instance().ifPresent(result::append);

            assertThat(result.toString()).isEmpty();
        }

        @Test
        void empty__returns_none_instance() {
            assertThat(Opt.empty()).isInstanceOf(None.class);
        }
    }
}
