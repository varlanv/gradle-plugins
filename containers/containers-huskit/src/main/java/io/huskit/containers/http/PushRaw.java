package io.huskit.containers.http;

import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.Optional;

public class PushRaw implements PushResponse<String> {

    @NonFinal
    @Nullable
    String body;

    @Override
    public Optional<String> value() {
        return Optional.ofNullable(body);
    }

    @Override
    public Optional<String> push(ByteBuffer byteBuffer) {
        if (body != null) {
            throw new IllegalStateException("Push response already set");
        }
        this.body = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
        return Optional.of(body);
    }
}
