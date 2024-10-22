package io.huskit.common.io;

import lombok.experimental.NonFinal;

import java.nio.CharBuffer;

public class FlexCharBuffer {

    @NonFinal
    CharBuffer delegate;

    public FlexCharBuffer(int capacity) {
        delegate = CharBuffer.allocate(capacity);
    }

    public void append(char c) {
        if (delegate.remaining() == 0) {
            grow();
        }
        delegate.put(c);
    }

    public String read() {
        delegate.flip();
        var string = delegate.toString();
        delegate.clear();
        return string;
    }

    public String readWithoutLastChar() {
        delegate.flip();
        var string = delegate.limit(delegate.limit() - 1).toString();
        delegate.clear();
        return string;
    }

    public void clear() {
        delegate.clear();
    }

    @Override
    public String toString() {
        return read();
    }

    private void grow() {
        var newBuffer = CharBuffer.allocate(delegate.capacity() * 2);
        delegate.flip();
        newBuffer.put(delegate);
        delegate = newBuffer;
    }
}
