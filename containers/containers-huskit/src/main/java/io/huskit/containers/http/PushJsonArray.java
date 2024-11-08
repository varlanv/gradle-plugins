package io.huskit.containers.http;

import io.huskit.containers.internal.HtJson;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PushJsonArray implements PushResponse<List<Map<String, Object>>> {

    PushResponse<List<Map<String, Object>>> delegate = new PushChunked<>(HtJson::toMapList);

    @Override
    public boolean isReady() {
        return delegate.isReady();
    }

    @Override
    public List<Map<String, Object>> value() {
        return delegate.value();
    }

    @Override
    public Optional<List<Map<String, Object>>> apply(ByteBuffer byteBuffer) {
        return delegate.apply(byteBuffer);
    }
}