package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtJsonContainer;
import io.huskit.containers.api.container.list.HtListContainers;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.PrintFormat;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@RequiredArgsConstructor
class HtHttpListContainers implements HtListContainers {

    HtHttpDockerSpec dockerSpec;
    HtHttpListContainersSpec spec;

    @PrintFormat
    private static final String requestFormat = "%s %s HTTP/1.1%n"
            + "Host: %s%n"
            + "Connection: keep-alive%n"
            + "%n";

    @Override
    public Stream<HtContainer> asStream() {
        return Stream.of(true)
                .flatMap(ignore -> {
                    var url = "/containers/json" + spec.toParameters();
                    var request = new DfHttpRequest(
                            String.format(
                                    requestFormat,
                                    "GET", url, "localhost"
                            ).getBytes(StandardCharsets.UTF_8)
                    );
                    return dockerSpec.socket().send(request, r -> HtJson.toMapList(r.reader()))
                            .body()
                            .stream()
                            .map(HtJsonContainer::new);
                });
    }
}
