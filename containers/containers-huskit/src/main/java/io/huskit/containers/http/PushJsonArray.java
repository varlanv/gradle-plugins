package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.containers.internal.HtJson;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class PushJsonArray implements PushResponse<List<Map<String, Object>>> {

    Mutable<List<Map<String, Object>>> response = Mutable.of();

    @Override
    public boolean isReady() {
        return response.isPresent();
    }

    @Override
    public List<Map<String, Object>> value() {
        return response.require();
    }

    @Override
    public Optional<List<Map<String, Object>>> apply(ByteBuffer byteBuffer) {
        if (response.isPresent()) {
            throw new IllegalStateException("Push response already set");
        } else {
            var mapList = HtJson.toMapList(
                new InputStreamReader(
                    new ByteArrayInputStream(
                        byteBuffer.array(),
                        byteBuffer.position(),
                        byteBuffer.remaining()
                    )
                )
            );
            response.set(mapList);
            return Optional.of(mapList);
        }
    }
}
