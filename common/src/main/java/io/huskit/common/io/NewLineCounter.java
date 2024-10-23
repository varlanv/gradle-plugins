package io.huskit.common.io;

import lombok.experimental.NonFinal;

import java.nio.IntBuffer;
import java.util.Arrays;

public final class NewLineCounter {

    @NonFinal
    IntBuffer positions;

    public NewLineCounter(byte[] array) {
        var buf = new int[10];
        var index = 0;
        for (var i = 0; i < array.length - 1; i++) {
            if (array[i] == '\r' && array[i + 1] == '\n') {
                if (buf.length == index) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[index++] = i;
            }
        }
        positions = IntBuffer.wrap(buf, 0, index);
    }

    public int nextPosition() {
        return positions.hasRemaining() ? positions.get() : -1;
    }

    IntBuffer positions() {
        return positions;
    }
}
