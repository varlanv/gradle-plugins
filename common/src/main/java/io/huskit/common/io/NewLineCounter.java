package io.huskit.common.io;

import lombok.experimental.NonFinal;

import java.util.Arrays;

public class NewLineCounter {

    byte[] array;
    @NonFinal
    int currentPosition = -1;
    @NonFinal
    int[] positions;

    public NewLineCounter(byte[] array) {
        this.array = array;
    }

    public int nextPosition() {
        positions();
        if (currentPosition == -1) {
            return -1;
        } else {
            if (positions.length < currentPosition + 1) {
                return -1;
            } else {
                return positions[currentPosition++];
            }
        }
    }

    int[] positions() {
        if (positions == null) {
            var buf = new int[30];
            var index = 0;
            for (var i = 0; i < array.length - 1; i++) {
                if (array[i] == '\r' && array[i + 1] == '\n') {
                    if (buf.length == index) {
                        buf = Arrays.copyOf(buf, buf.length * 2);
                    }
                    buf[index++] = i;
                }
            }
            positions = Arrays.copyOf(buf, index);
            currentPosition = positions.length > 0 ? 0 : -1;
        }
        return positions;
    }
}
