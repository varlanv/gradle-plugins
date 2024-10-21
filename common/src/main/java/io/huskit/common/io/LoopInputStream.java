package io.huskit.common.io;

import io.huskit.common.function.ThrowingSupplier;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;

import java.io.IOException;
import java.io.InputStream;

public final class LoopInputStream extends InputStream {

    @NonFinal
    InputStream delegate;
    @NonFinal
    int readCount;
    @NonFinal
    boolean isBroken;
    ThrowingSupplier<InputStream> delegateSupplier;

    @SneakyThrows
    public LoopInputStream(ThrowingSupplier<InputStream> delegateSupplier) {
        this.delegate = delegateSupplier.get();
        this.delegateSupplier = delegateSupplier;
    }

    @Override
    @SneakyThrows
    public int read() {
        if (isBroken) {
            return -1;
        }
        var i = delegate.read();
        if (i == -1) {
            if (readCount == 0) {
                isBroken = true;
                return -1;
            }
            delegate = delegateSupplier.get();
            return read();
        } else {
            readCount++;
        }
        return i;
    }

    @Override
    public int read(byte[] bytes, int off, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        throw new UnsupportedOperationException();
    }
}
