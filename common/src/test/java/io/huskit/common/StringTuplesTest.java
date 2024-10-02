package io.huskit.common;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StringTuplesTest implements UnitTest {

    @Test
    void toList__empty() {
        var subject = new StringTuples();

        var actual = subject.toList();

        assertThat(actual).isEmpty();
    }

    @Test
    void toList__varargs__1() {
        var subject = new StringTuples("a");

        var actual = subject.toList();

        assertThat(actual).containsExactly("a");
    }

    @Test
    void toList__varargs__2() {
        var subject = new StringTuples("a", "b");

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b");
    }

    @Test
    void toList__varargs__3() {
        var subject = new StringTuples("a", "b", "c");

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b", "c");
    }

    @Test
    void toList__varargs__11() {
        var subject = new StringTuples("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");

        var actual = subject.toList();

        assertThat(actual).containsExactly("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
    }

    @Test
    void toList__collection__1() {
        var subject = new StringTuples(List.of("a"));

        var actual = subject.toList();

        assertThat(actual).containsExactly("a");
    }

    @Test
    void toList__collection__2() {
        var subject = new StringTuples(List.of("a", "b"));

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b");
    }

    @Test
    void toList__collection__3() {
        var subject = new StringTuples(List.of("a", "b", "c"));

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b", "c");
    }

    @Test
    void toList__when_adding__existing_tuple__then_no_duplicates() {
        var subject = new StringTuples("a", "b");
        subject.add("a", "b");

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b");
    }

    @Test
    void toList__when_adding__key_that_exists_in_tuple_pair__then_adds_key() {
        var subject = new StringTuples("a", "b");
        subject.add("a");

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b", "a");
    }

    @Test
    void toList__when_adding__key_that_exists_in_tuple_pair__then_adds_value() {
        var subject = new StringTuples("a", "b", "c");
        subject.add("c");

        var actual = subject.toList();

        assertThat(actual).containsExactly("a", "b", "c");
    }
}
