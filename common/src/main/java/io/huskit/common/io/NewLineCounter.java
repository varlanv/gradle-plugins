package io.huskit.common.io;

import org.jetbrains.annotations.VisibleForTesting;

import java.nio.IntBuffer;
import java.util.Arrays;

public final class NewLineCounter {

    IntBuffer positions;
    int initialPosition;

    public NewLineCounter(byte[] array, Integer initialPosition) {
        if (initialPosition < 0 || initialPosition > array.length) {
            throw new IllegalArgumentException("Initial position must be between 0 and array length");
        }
        this.initialPosition = initialPosition;
        this.positions = initPositions(array);
    }

    public NewLineCounter(byte[] array) {
        this(array, 0);
    }

    public int nextPosition() {
        return positions.hasRemaining() ? positions.get() : -1;
    }

    @VisibleForTesting
    IntBuffer positions() {
        return positions;
    }

    private IntBuffer initPositions(byte[] array) {
        final IntBuffer positions;
        if (array.length == 0) {
            positions = IntBuffer.allocate(0);
            return positions;
        }
        var buf = new int[10];
        var index = 0;
        byte prev = -1;
        for (var i = initialPosition; i < array.length - 1; i += 2) {
            var curr = array[i];
            var next = array[i + 1];
            if (curr == '\r' && next == '\n') {
                if (buf.length == index) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[index++] = i;
            } else if (curr == '\n' && prev == '\r') {
                if (buf.length == index) {
                    buf = Arrays.copyOf(buf, buf.length * 2);
                }
                buf[index++] = i - 1;
            }
            prev = next;
        }
        if (array[array.length - 1] == '\n' && prev == '\r') {
            if (buf.length == index) {
                buf = Arrays.copyOf(buf, buf.length * 2);
            }
            buf[index++] = array.length - 2;
        }
        positions = IntBuffer.wrap(buf, 0, index);
        return positions;
    }
}
