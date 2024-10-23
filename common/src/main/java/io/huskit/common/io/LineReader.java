package io.huskit.common.io;

import lombok.experimental.NonFinal;

import java.util.function.Supplier;

public class LineReader {

    private static final int NEST_LIMIT = 1000;
    Supplier<byte[]> bufferSupplier;
    int nestLimit;
    @NonFinal
    byte[] currentBuffer;
    @NonFinal
    int currentBufferIndex;

    public LineReader(Supplier<byte[]> bufferSupplier) {
        this(bufferSupplier, NEST_LIMIT);
    }

    public LineReader(Supplier<byte[]> bufferSupplier, int nestLimit) {
        this.bufferSupplier = bufferSupplier;
        this.nestLimit = nestLimit;
    }

    public String readLine() {
        if (currentBuffer == null) {
            currentBuffer = bufferSupplier.get();
        }
        String res = null;
        var previousIndex = currentBufferIndex;
        var nestCount = 0;
        byte prevByte = 0;
        while (res == null) {
            var bytes = currentBuffer;
            var currentIndex = previousIndex;
            var isContinue = true;
            var prev = prevByte;
            var currentOffset = 0;
            for (var i = previousIndex; i < bytes.length && isContinue; i++) {
                var curr = bytes[i];
                isContinue = !(curr == '\n' && prev == '\r');
                currentIndex = i;
                prev = curr;
                currentOffset++;
            }
            if (isContinue) {
                if (nestCount >= nestLimit) {
                    throw new IllegalStateException("Couldn't find new line after " + NEST_LIMIT + " reads");
                }
                var newBuf = bufferSupplier.get();
                var mergedBuf = new byte[bytes.length + newBuf.length];
                System.arraycopy(bytes, currentBufferIndex, mergedBuf, 0, bytes.length - currentBufferIndex);
                System.arraycopy(newBuf, 0, mergedBuf, bytes.length - currentBufferIndex, newBuf.length);
                currentBuffer = mergedBuf;
                nestCount++;
            } else {
                var line = new String(bytes, currentBufferIndex, currentOffset - 2);
                currentBufferIndex = currentIndex + 1;
                if (currentBufferIndex >= bytes.length) {
                    currentBuffer = null;
                    currentBufferIndex = 0;
                }
                res = line;
            }
        }
        return res;
    }
}
