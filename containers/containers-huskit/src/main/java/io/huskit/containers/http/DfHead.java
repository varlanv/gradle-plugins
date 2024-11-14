package io.huskit.containers.http;

import java.util.Map;

record DfHead(Integer status,
              Map<String, String> headers,
              Boolean isChunked,
              Boolean isMultiplexedStream) implements Http.Head {
}
