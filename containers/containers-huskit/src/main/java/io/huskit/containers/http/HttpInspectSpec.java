package io.huskit.containers.http;

import org.intellij.lang.annotations.PrintFormat;

import java.nio.charset.StandardCharsets;

public class HttpInspectSpec {

    @PrintFormat
    private static final String requestFormat = "%s %s HTTP/1.1%n"
            + "Host: %s%n"
            + "Connection: keep-alive%n"
            + "%n";
    String id;

    public HttpInspectSpec(CharSequence id) {
        this.id = id.toString();
    }

    public Http.Request toRequest() {
        return new DfHttpRequest(
                String.format(requestFormat, "GET", "/containers/" + id + "/json", "localhost")
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
