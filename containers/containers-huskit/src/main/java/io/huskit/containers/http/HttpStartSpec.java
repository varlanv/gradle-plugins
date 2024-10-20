package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@Getter
@RequiredArgsConstructor
public class HttpStartSpec {

    private static final String requestFormat = "%s %s HTTP/1.1%n"
            + "Host: %s%n"
            + "Connection: keep-alive%n"
            + "Content-Type: application/json%n"
            + "%n";

    public Http.Request toRequest(String containerId) {
        return new DfHttpRequest(
                String.format(requestFormat, "POST", "/containers/" + containerId + "/start", "localhost")
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
