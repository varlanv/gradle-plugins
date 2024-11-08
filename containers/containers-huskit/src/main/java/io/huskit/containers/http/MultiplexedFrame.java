package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@Getter
@RequiredArgsConstructor
public final class MultiplexedFrame {

    byte[] data;
    FrameType type;

    public String stringData() {
        return new String(data, StandardCharsets.UTF_8).trim();
    }

    @Override
    public String toString() {
        var frame = new String(data, StandardCharsets.UTF_8).trim();
        return "MultiplexedFrame{"
            + "type=" + type
            + ", data='" + frame + '\''
            + '}';
    }
}