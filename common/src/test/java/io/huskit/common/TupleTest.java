package io.huskit.common;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TupleTest implements UnitTest {

    @Test
    void toList__returns_list() {
        var subject = Tuple.of("left", "right");

        var result = subject.toList();

        assertEquals(2, result.size());
        assertEquals("left", result.get(0));
        assertEquals("right", result.get(1));
    }

    @Test
    void of__when_left_null__throws_exception() {
        var right = "right";

        assertThrows(NullPointerException.class, () -> Tuple.of(null, right));
    }

    @Test
    void of__when_right_null__throws_exception() {
        var left = "left";

        assertThrows(NullPointerException.class, () -> Tuple.of(left, null));
    }

    @Test
    void of__when_both_null__throws_exception() {
        assertThrows(NullPointerException.class, () -> Tuple.of(null, null));
    }
}
