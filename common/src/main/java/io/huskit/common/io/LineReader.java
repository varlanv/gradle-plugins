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
    NewLineCounter currentLineCounter;
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
            currentLineCounter = new NewLineCounter(currentBuffer);
        }
        String res = null;

        var idx = currentLineCounter.nextPosition();

        res = new String(currentBuffer, currentBufferIndex, idx - currentBufferIndex);
        currentBufferIndex = idx + 2;
        return res;
    }
}
