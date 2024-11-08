package io.huskit.containers.http;

import io.huskit.containers.internal.HtJson;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;

final class PushJsonObject implements PushResponse<Map<String, Object>> {

    PushResponse<Map<String, Object>> delegate = new PushChunked<>(HtJson::toMap);

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public Map<String, Object> value() {
        return delegate.value();
    }

    @Override
    public Optional<Map<String, Object>> apply(ByteBuffer byteBuffer) {
        return delegate.apply(byteBuffer);
    }
}