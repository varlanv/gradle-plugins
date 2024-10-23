package io.huskit.common.io;

import java.nio.IntBuffer;
import java.util.Arrays;

public final class NewLineCounter {

    IntBuffer positions;

    public NewLineCounter(byte[] array) {
        if (array.length == 0) {
            positions = IntBuffer.allocate(0);
            return;
        }
        var buf = new int[10];
        var index = 0;
        byte prev = -1;
        for (var i = 0; i < array.length - 1; i += 2) {
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
    }

    public int nextPosition() {
        return positions.hasRemaining() ? positions.get() : -1;
    }

    IntBuffer positions() {
        return positions;
    }
}
