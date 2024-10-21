package io.huskit.common.io;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.InputStream;
import java.nio.ByteBuffer;

@RequiredArgsConstructor
public final class ByteBufferInputStream extends InputStream {

    @NonNull
    ByteBuffer buffer;

    @Override
    public int read() {
        if (buffer.hasRemaining()) {
            return buffer.get() & 0xFF;
        } else {
            return -1;
        }
    }

    @Override
    public int read(byte[] bytes, int off, int len) {
        if (!buffer.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }
}