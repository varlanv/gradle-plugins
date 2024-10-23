package io.huskit.common.io;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

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
}
