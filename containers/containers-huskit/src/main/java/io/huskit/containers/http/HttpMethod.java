package io.huskit.containers.http;

import java.nio.charset.StandardCharsets;

public enum HttpMethod {

    GET,
    POST,
    PUT,
    DELETE;

    private byte[] bytes;

    HttpMethod() {
        this.bytes = (name() + " ").getBytes(StandardCharsets.UTF_8);
    }

    byte[] bytes() {
        return bytes;
    }
}