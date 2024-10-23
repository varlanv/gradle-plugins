package io.huskit.common.io;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class NewLineCounterTest implements UnitTest {

    @Test
    void performance_check() {
        var bytes = "qwerty\r\n".repeat(100000).getBytes(StandardCharsets.UTF_8);
        microBenchmark(() -> NewLineCounter.positions(bytes));
    }

    @MethodSource
    @ParameterizedTest
    void positions__should_return_positions_of_new_lines(String input, int[] expected) {
        var array = input.getBytes(StandardCharsets.UTF_8);
        var res = NewLineCounter.positions(array);
        assertThat(res).containsExactly(expected);
    }

    Stream<Arguments> positions__should_return_positions_of_new_lines() {
        return Stream.of(
                Arguments.of(
                        "a\r\nb\r\nc\r\nd", new int[]{1, 4, 7},
                        "\r\n\r\n", new int[]{1},
                        "", new int[]{},
                        "\r\n", new int[]{1},
                        "\n", new int[]{},
                        "a\r\n\r\nb\r\nc", new int[]{1, 4, 6},
                        "a\r\n\r\nb\r\nc\r\n", new int[]{1, 4, 6, 9}
                )
        );
    }
}
