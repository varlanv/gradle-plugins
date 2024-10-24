package io.huskit.common.io;

import lombok.experimental.NonFinal;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

public final class BufferLines implements Lines {

    private static final int NEST_LIMIT = 1000;
    Supplier<byte[]> bufferSupplier;
    int nestLimit;
    @NonFinal
    byte[] currentBuffer;
    @NonFinal
    NewLineCounter currentLineCounter;
    @NonFinal
    int currentBufferIndex;

    public BufferLines(Supplier<byte[]> bufferSupplier) {
        this(bufferSupplier, NEST_LIMIT);
    }

    public BufferLines(Supplier<byte[]> bufferSupplier, int nestLimit) {
        this.bufferSupplier = bufferSupplier;
        this.nestLimit = nestLimit;
        this.currentBuffer = bufferSupplier.get();
        this.currentLineCounter = new NewLineCounter(currentBuffer);

    }

    @Override
    public Line next() {
        String res = null;
        var currentNest = 0;
        while (res == null) {
            var idx = currentLineCounter.nextPosition();
            if (idx == -1) {
                if (currentNest++ > nestLimit) {
                    throw new IllegalStateException(String.format("Couldn't find new line after %s reads", nestLimit));
                }
                var oldBuffer = currentBuffer;
                var newBuffer = bufferSupplier.get();
                if (newBuffer.length == 0) {
                    continue;
                }
                currentBuffer = Arrays.copyOf(oldBuffer, oldBuffer.length + newBuffer.length);
                System.arraycopy(newBuffer, 0, currentBuffer, oldBuffer.length, newBuffer.length);
                currentLineCounter = new NewLineCounter(currentBuffer, currentBufferIndex);
            } else {
                res = new String(currentBuffer, currentBufferIndex, idx - currentBufferIndex);
                currentBufferIndex = idx + 2;
            }
        }
        return new Line(res);
    }
}
