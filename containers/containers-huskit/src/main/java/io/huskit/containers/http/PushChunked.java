package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.common.number.Hexadecimal;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

@RequiredArgsConstructor
final class PushChunked<T> implements PushResponse<T> {

    Function<String, T> delegate;
    Mutable<T> body = Mutable.of();
    StringBuilder fullBody = new StringBuilder(256);
    @NonFinal
    int skipNext = 0;
    @NonFinal
    boolean isChunkSizePart = true;
    Hexadecimal currentChunkSizeHex = Hexadecimal.fromHexChars();

    @Override
    public boolean isReady() {
        return body.isPresent();
    }

    @Override
    public T value() {
        return body.require();
    }

    @Override
    public Optional<T> apply(ByteBuffer byteBuffer) {
        while (byteBuffer.hasRemaining()) {
            var b = byteBuffer.get();
            var ch = (char) (b & 0xFF);
            if (skipNext > 0) {
                skipNext--;
                continue;
            }
            if (isChunkSizePart) {
                if (ch == '\n') {
                    continue;
                }
                if (ch == '\r') {
                    isChunkSizePart = false;
                    if (currentChunkSizeHex.intValue() == 0) {
                        var value = delegate.apply(fullBody.toString());
                        body.set(value);
                        return Optional.of(value);
                    } else {
                        skipNext = 1;
                    }
                } else {
                    currentChunkSizeHex.withHexChar(ch);
                }
            } else {
                var chunkSize = currentChunkSizeHex.decrement().intValue();
                fullBody.append(ch);
                if (chunkSize == 0) {
                    isChunkSizePart = true;
                    skipNext = 2;
                }
            }
        }
        return Optional.empty();
    }
}