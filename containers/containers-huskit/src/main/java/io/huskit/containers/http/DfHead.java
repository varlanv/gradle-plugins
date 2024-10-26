package io.huskit.containers.http;

import lombok.Getter;

import java.util.Map;

@Getter
final class DfHead implements Http.Head {

    Integer status;
    Map<String, String> headers;
    Boolean isChunked;
    Boolean isMultiplexedStream;

    DfHead(Integer status, Map<String, String> headers) {
        this(status, headers, null, null);
    }

    DfHead(Integer status, Map<String, String> headers, Boolean isChunked, Boolean isMultiplexedStream) {
        this.status = status;
        this.headers = headers;
        this.isChunked = isChunked;
        this.isMultiplexedStream = isMultiplexedStream;
    }

    @Override
    public Boolean isChunked() {
        return isChunked != null ?
                isChunked :
                "chunked".equals(headers().get("Transfer-Encoding"));
    }

    @Override
    public Boolean isMultiplexedStream() {
        return isMultiplexedStream != null ?
                isMultiplexedStream :
                "application/vnd.docker.multiplexed-stream".equals(headers().get("Content-Type"));
    }
}
