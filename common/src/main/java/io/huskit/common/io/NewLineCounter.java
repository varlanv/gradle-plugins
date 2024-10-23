package io.huskit.common.io;

import java.util.Arrays;

public class NewLineCounter {

    static int[] positions(byte[] array) {
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
        return Arrays.copyOf(buf, index);
    }
}
