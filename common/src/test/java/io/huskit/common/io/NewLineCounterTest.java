package io.huskit.common.io;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NewLineCounterTest implements UnitTest {

    @Test
    void performance_check() {
        var bytes = "qwerty\r\n".repeat(100000).getBytes(StandardCharsets.UTF_8);
        microBenchmark(200, () -> new NewLineCounter(bytes).positions());
    }

    @MethodSource
    @ParameterizedTest
    void positions__should_return_positions_of_new_lines(String input, int[] expected) {
        verify(input, expected);
    }

    @Test
    void positions_qwerty_repeated_50() {
        verify(
                "qwerty\r\n".repeat(50),
                IntStream.range(0, 50).map(i -> i * 8 + 6).toArray()
        );
    }

    @Test
    void positions_qwerty_repeated_3() {
        verify(
                "qwerty\r\nqwerty\r\nqwerty\r\n",
                new int[]{6, 14, 22}
        );
    }

    @Test
    void positions_qwerty_repeated_3_with_new_line_at_start() {
        verify(
                "\r\nqwerty\r\nqwerty\r\nqwerty\r\n",
                new int[]{0, 8, 16, 24}
        );
    }

    @Test
    void positions_qwertyu_repeated_50() {
        var input = "qwertyu\r\n".repeat(50);
        var expected = IntStream.range(0, 50)
                .map(i -> i * 9 + 7)
                .toArray();

        verify(
                input,
                expected
        );
    }

    @Test
    void positions_qwertyu_repeated_3() {
        verify(
                "qwertyu\r\nqwertyu\r\nqwertyu\r\n",
                new int[]{7, 16, 25}
        );
    }

    @Test
    void positions_qwertyu_repeated_3_with_new_line_at_start() {
        verify(
                "\r\nqwertyu\r\nqwertyu\r\nqwertyu\r\n",
                new int[]{0, 9, 18, 27}
        );
    }

    private Stream<Arguments> positions__should_return_positions_of_new_lines() {
        return Stream.of(
                Arguments.of("a\r\nb\r\nc\r\nd", new int[]{1, 4, 7}),
                Arguments.of("\r\n\r\n", new int[]{0, 2}),
                Arguments.of("", new int[]{}),
                Arguments.of("\r\n", new int[]{0}),
                Arguments.of("\n", new int[]{}),
                Arguments.of("a\r\n\r\nb\r\nc", new int[]{1, 3, 6}),
                Arguments.of("a\r\n\r\nb\r\nc\r\n", new int[]{1, 3, 6, 9})
        );
    }

    private void verify(String input, int[] expected) {
        var array = input.getBytes(StandardCharsets.UTF_8);
        var subject = new NewLineCounter(array);
        var res = subject.positions();
        assertThat(res).containsExactly(expected);

        for (int pos : expected) {
            assertThat(subject.nextPosition()).isEqualTo(pos);
        }
        assertThat(subject.nextPosition()).isEqualTo(-1);
    }
}
