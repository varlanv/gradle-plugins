package io.huskit.containers.http;

import java.nio.charset.StandardCharsets;

public class HttpRequests {

    byte[] afterUrlPart;

    public HttpRequests() {
        this.afterUrlPart = (" "
                + "HTTP/1.1\r\n"
                + "Host: localhost\r\n"
                + "Connection: keep-alive\r\n"
                + "Content-Type: application/json\r\n"
                + "\r\n"
        ).getBytes(StandardCharsets.UTF_8);
    }

    public HttpRequestWith generate(HttpMethod method, String url) {
        var urlBytes = url.getBytes(StandardCharsets.UTF_8);
        return new HttpRequestWith() {
            @Override
            public Http.Request body(byte[] body) {
                return new DfHttpRequest(
                        method.bytes(),
                        urlBytes,
                        afterUrlPart,
                        body
                );
            }

            @Override
            public Http.Request emptyBody() {
                return new DfHttpRequest(
                        method.bytes(),
                        urlBytes,
                        afterUrlPart
                );
            }
        };
    }

    public interface HttpRequestWith {

        Http.Request body(byte[] body);

        Http.Request emptyBody();
    }
}
