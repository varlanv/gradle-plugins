package io.huskit.containers.http;

import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.util.Optional;

public class PushRaw implements PushResponse<String> {

    @NonFinal
    String body;

    @Override
    public boolean isReady() {
        return body != null;
    }

    @Override
    public String value() {
        if (!isReady()) {
            throw new IllegalStateException("Not ready");
        }
        return body;
    }

    @Override
    public Optional<String> apply(ByteBuffer byteBuffer) {
        this.body = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.remaining());
        return Optional.of(body);
    }
}
