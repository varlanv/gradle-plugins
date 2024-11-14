package io.huskit.common.collection;

import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;

public final class FlexBytes {

    @NonFinal
    ByteBuffer bytes;
    @NonFinal
    int position;

    public FlexBytes(Integer initialCapacity) {
        this.bytes = ByteBuffer.allocate(initialCapacity);
    }

    public FlexBytes() {
        this(256);
    }

    public FlexBytes append(byte b) {
        if (bytes.remaining() == 0) {
            var newBytes = ByteBuffer.allocate(bytes.capacity() * 2);
            bytes.flip();
            newBytes.put(bytes);
            bytes = newBytes;
        }
        bytes.put(b);
        return this;
    }

    public ByteBuffer buffer() {
        return bytes;
    }
}
