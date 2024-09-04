package io.huskit.gradle.common.plugin.model.string;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapitalizedStringTest implements UnitTest {

    @MethodSource
    @ParameterizedTest
    @DisplayName("should capitalize the first letter of a string")
    void test_0(String expected, String actual) {
        assertThat(new CapitalizedString(expected).toString()).isEqualTo(actual);
    }

    static Stream<Arguments> test_0() {
        return Stream.of(
                Arguments.of("hello", "Hello"),
                Arguments.of("123", "123"),
                Arguments.of("HELLO", "HELLO"),
                Arguments.of("hELLO", "HELLO"),
                Arguments.of("Hello", "Hello")
        );
    }

    @Test
    @DisplayName("null shouldn't be handled")
    void test_1() {
        assertThatThrownBy(() -> new CapitalizedString(null))
                .isInstanceOf(NullPointerException.class);
    }
}
