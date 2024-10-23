package io.huskit.common.io;

import io.huskit.common.function.ThrowingSupplier;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
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
//        compareWithBufferedReader("abc", "qwerty".repeat(100000), "asdfg".repeat(100));
        var linesCount = 300;
        var linesSize = 1000;
        var lines = new String[linesCount];
        IntStream.range(0, linesCount)
                .forEach(i -> lines[i] = String.valueOf(i).repeat(linesSize));
        compareWithBufferedReader(lines);
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
            var linesQueue = new ArrayDeque<>(linesList);
            var lineReader = new LineReader(() -> linesQueue.poll().getBytes(StandardCharsets.UTF_8));
            var linesQueueSize = linesQueue.size();
            var iterationsCount = new AtomicInteger();
            var nanos = System.nanoTime();
            for (var i = 0; i < linesQueueSize; i++) {
                assertThat(lineReader.readLine()).isNotEmpty();
                iterationsCount.incrementAndGet();
            }
            assertThat(iterationsCount.get()).isEqualTo(linesList.size());
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
            assertThat(iterationsCount.get()).isEqualTo(linesList.size());
            return (System.nanoTime() - nanos) / 1000;
        };

        lineReaderTimeMicros.get();
        bufferedTimeMicros.get();


        var lineReaderTimeAverage = IntStream.range(0, 10)
                .mapToLong(i -> {
                    try {
                        return lineReaderTimeMicros.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .average()
                .orElseThrow();
        var bufferedTimeAverage = IntStream.range(0, 100)
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
