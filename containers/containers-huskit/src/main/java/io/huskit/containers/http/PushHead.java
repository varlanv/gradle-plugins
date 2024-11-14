package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.common.io.Lines;
import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class PushHead implements PushResponse<Http.Head> {

    Mutable<Http.Head> head = Mutable.of();
    List<String> lines = new ArrayList<>();
    @NonFinal
    StringBuilder lineBuilder = new StringBuilder(64);
    @NonFinal
    char previousChar;

    @Override
    public Optional<Http.Head> value() {
        return head.maybe();
    }

    @Override
    public synchronized Optional<Http.Head> push(ByteBuffer byteBuffer) {
        if (head.isPresent()) {
            return head.maybe();
        }
        while (byteBuffer.hasRemaining()) {
            var currentChar = (char) (byteBuffer.get() & 0xFF);
            if (currentChar == '\n' && previousChar == '\r') {
                if (lineBuilder.length() == 1) {
                    var head = new HeadFromLines(Lines.fromIterable(lines));
                    this.head.set(head);
                    return Optional.of(head);
                } else {
                    lineBuilder.deleteCharAt(lineBuilder.length() - 1);
                    lines.add(lineBuilder.toString());
                    lineBuilder = new StringBuilder(64);
                }
            } else {
                lineBuilder.append(currentChar);
            }
            previousChar = currentChar;
        }
        return Optional.empty();
    }
}