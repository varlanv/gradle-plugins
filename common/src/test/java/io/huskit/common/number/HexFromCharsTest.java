package io.huskit.common.number;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HexFromCharsTest implements UnitTest {

    @Test
    void intValue__when_single_hex_char__then_return_value() {
        var subject = Hexadecimal.fromHexChars();

        assertThat(subject.intValue()).isZero();
    }

    @Test
    void intValue__withZeroHexChar__then_returnZero() {
        var subject = Hexadecimal.fromHexChars()
                .withHexChar('0');

        assertThat(subject.intValue()).isZero();
    }

    @Test
    void intValue_with_b0__then_return_176() {
        var subject = Hexadecimal.fromHexChars()
                .withHexChar('b')
                .withHexChar('0');

        assertThat(subject.intValue()).isEqualTo(176);
    }

    @Test
    void intValue__with_1ca__then_return_458() {
        var subject = Hexadecimal.fromHexChars()
                .withHexChar('1')
                .withHexChar('c')
                .withHexChar('a');

        assertThat(subject.intValue()).isEqualTo(458);
    }
}
