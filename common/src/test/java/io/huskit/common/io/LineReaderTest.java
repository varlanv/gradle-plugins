package io.huskit.common.io;

import io.huskit.common.function.ThrowingSupplier;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class LineReaderTest implements UnitTest {

    @Test
    void when_new_line_not_found_within_nest_limit__throw_exception() {
        var nestLimit = 1000;
        var subject = new LineReader(() -> "asd".getBytes(StandardCharsets.UTF_8), nestLimit);
        assertThatThrownBy(subject::readLine)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Couldn't find new line after %s reads", nestLimit);
    }

    @Test
    void when_input_contains_only_crlf__then_return_empty_line() {
        var subject = new LineReader(() -> "\r\n".getBytes(StandardCharsets.UTF_8));

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("");
    }

    @Test
    void when_input_contains_only_crlf_split_between_two_inputs__then_return_empty_line() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "\r".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("");
    }

    @Test
    void when_input_contains_only_1_letter_and_then_crlf__then_return_1_letter() {
        var subject = new LineReader(() -> "q\r\n".getBytes(StandardCharsets.UTF_8));

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("q");
    }

    @Test
    void when_read_many_lines__should_work_correctly() {
        var linesCount = 20;
        var wordBase = "qwerty";
        var bytes = IntStream.rangeClosed(0, linesCount)
                .mapToObj(i -> "qwerty" + i + "\r\n")
                .collect(Collectors.joining())
                .getBytes(StandardCharsets.UTF_8);

        var subject = new LineReader(() -> bytes);

        for (var i = 0; i < linesCount; i++) {
            assertThat(subject.readLine()).isEqualTo(wordBase + i);
        }
    }

    @Test
    void when_input_contains_two_lines_in_same_input__then_should_read_it_correctly() {
        var subject = new LineReader(() -> "qwe\r\nrty\r\n".getBytes(StandardCharsets.UTF_8));

        assertThat(subject.readLine()).isEqualTo("qwe");
        assertThat(subject.readLine()).isEqualTo("rty");
    }

    @Test
    void when_input_contains_two_lines_in_same_input_and_second_line_is_empty__then_should_read_it_correctly() {
        var subject = new LineReader(() -> "qwe\r\n\r\n".getBytes(StandardCharsets.UTF_8));

        assertThat(subject.readLine()).isEqualTo("qwe");
        assertThat(subject.readLine()).isEqualTo("");
        assertThat(subject.readLine()).isEqualTo("qwe");
        assertThat(subject.readLine()).isEqualTo("");
    }

    @Test
    void when_input_contains_two_lines_in_first_input_and_one_empty_line_split_be_two_next_inputs__then_should_read_it_correctly() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.incrementAndGet() == 1) {
                return "qwe\r\n\r\n".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 2) {
                return "\r".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        assertThat(subject.readLine()).isEqualTo("qwe");
        assertThat(subject.readLine()).isEqualTo("");
        assertThat(subject.readLine()).isEqualTo("");
    }

    @Test
    void when_input_contains_4_letters_and_then_crlf__then_return_1_letter() {
        var subject = new LineReader(() -> "asdf\r\n".getBytes(StandardCharsets.UTF_8));

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("asdf");
    }

    @Test
    void when_input_contains_4_letters_and_then_crlf_inputs__then_return_1_letter() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "asdf".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\r\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("asdf");
    }

    @Test
    void when_input_contains_2_crlf_split_between_4_inputs__then_return_2_empty_lines() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "\r".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 2) {
                return "\n".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 3) {
                return "\r".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 4) {
                return "\n".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\r".getBytes(StandardCharsets.UTF_8);
            }
        });

        assertThat(subject.readLine()).isEqualTo("");
        assertThat(subject.readLine()).isEqualTo("");
        assertThatThrownBy(subject::readLine)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void when_input_contains_only_1_letter_and_then_crlf_split_between_two_inputs__then_return_1_letter() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "q".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 2) {
                return "\r".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("q");
    }

    @Test
    void when_input_contains_only_asdf_letter_and_then_crlf_split_between_two_inputs__then_return_1_letter() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "asdf".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 2) {
                return "\r".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("asdf");
    }

    @Test
    void when_new_line_found_on_first_read__return_line() {
        var subject = new LineReader(() -> "qwe\r\n".getBytes(StandardCharsets.UTF_8));

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("qwe");
    }

    @Test
    void when_new_line_found_on_second_read__return_line() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "qwe".getBytes(StandardCharsets.UTF_8);
            } else {
                return "rty\r\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("qwerty");
    }

    @Test
    void when_new_line_found_on_third_read__return_line() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "qwe".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 2) {
                return "rty".getBytes(StandardCharsets.UTF_8);
            } else {
                return "uio\r\n".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("qwertyuio");
    }

    @Test
    void should_be_able_to_find_three_new_lines() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "qwe\r\n".getBytes(StandardCharsets.UTF_8);
            } else if (counter.get() == 2) {
                return "rty\r\n".getBytes(StandardCharsets.UTF_8);
            } else {
                return "uio\r\n".getBytes(StandardCharsets.UTF_8);
            }
        });


        assertThat(subject.readLine()).isEqualTo("qwe");
        assertThat(subject.readLine()).isEqualTo("rty");
        assertThat(subject.readLine()).isEqualTo("uio");
    }

    @Test
    void when_new_line_cr_lf_is_spit_between_two_buffers__return_line() {
        var counter = new AtomicInteger();
        var subject = new LineReader(() -> {
            if (counter.getAndIncrement() == 0) {
                return "qwe\r".getBytes(StandardCharsets.UTF_8);
            } else {
                return "\nrty".getBytes(StandardCharsets.UTF_8);
            }
        });

        var actual = subject.readLine();

        assertThat(actual).isEqualTo("qwe");
    }

    @Test
    void buffered_reader_performance_test_big_lines() throws Exception {
        var linesCount = 150;
        var linesSize = 1000;
        compareWithBufferedReader(
                IntStream.range(0, linesCount)
                        .mapToObj(i -> String.valueOf(i).repeat(linesSize))
                        .toArray(String[]::new)
        );
    }

    @Test
    void buffered_reader_performance_test_avg_lines() throws Exception {
        var linesCount = 40;
        var linesSize = 30;
        compareWithBufferedReader(
                IntStream.range(0, linesCount)
                        .mapToObj(i -> String.valueOf(i).repeat(linesSize))
                        .toArray(String[]::new)
        );
    }

    private void compareWithBufferedReader(String... lines) throws Exception {
        var linesList = Arrays.stream(lines)
                .map(line -> line + "\r\n")
                .collect(Collectors.toList());
        var bytes = linesList.stream()
                .map(line -> line.getBytes(StandardCharsets.UTF_8))
                .reduce(new byte[0], (acc, line) -> {
                    var newBytes = new byte[acc.length + line.length];
                    System.arraycopy(acc, 0, newBytes, 0, acc.length);
                    System.arraycopy(line, 0, newBytes, acc.length, line.length);
                    return newBytes;
                });

        ThrowingSupplier<Long> lineReaderTimeMicros = () -> {
            var lineReader = new LineReader(() -> bytes);
            var iterationsCount = new AtomicInteger();
            var nanos = System.nanoTime();
            for (var i = 0; i < lines.length; i++) {
                assertThat(lineReader.readLine()).isNotEmpty();
                iterationsCount.incrementAndGet();
            }
            assertThat(iterationsCount.get()).isEqualTo(lines.length);
            return (System.nanoTime() - nanos) / 1000;
        };
        ThrowingSupplier<Long> bufferedTimeMicros = () -> {
            var bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
            var iterationsCount = new AtomicInteger();
            var nanos = System.nanoTime();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                assertThat(line).isNotEmpty();
                iterationsCount.incrementAndGet();
            }
            assertThat(iterationsCount.get()).isEqualTo(lines.length);
            return (System.nanoTime() - nanos) / 1000;
        };

        lineReaderTimeMicros.get();
        bufferedTimeMicros.get();

        var iterationsCount = 1000;
        var lineReaderTimeAverage = IntStream.range(0, iterationsCount)
                .mapToLong(i -> {
                    try {
                        return lineReaderTimeMicros.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .average()
                .orElseThrow();
        var bufferedTimeAverage = IntStream.range(0, iterationsCount)
                .mapToLong(i -> {
                    try {
                        return bufferedTimeMicros.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .average()
                .orElseThrow();

        System.out.printf("%nLines size - %s, Lines length - %s%n" +
                        "LineReader: %s micros%n" +
                        "BufferedReader: %s micros%n%n",
                lines.length, lines.length > 0 ? lines[0].length() : 0, lineReaderTimeAverage, bufferedTimeAverage
        );
    }
}
