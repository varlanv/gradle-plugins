package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtJsonContainer;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class HttpInspect {

    HtHttpDockerSpec dockerSpec;

    public HtContainer inspect(CharSequence id) {
        var response = dockerSpec.socket().send(new HttpInspectSpec(id).toRequest(), r -> HtJson.toMapList(r.reader()));
        var status = response.head().status();
        if (status != 200) {
            throw new RuntimeException(String.format("Failed to inspect container, received status %s - %s", status, response.body().list()));
        }
        return new HtJsonContainer(response.body().single());
    }

    public Stream<HtContainer> inspect(Iterable<? extends CharSequence> containerIds) {
        return Stream.of(containerIds)
                .flatMap(ids -> {
                    var url = "/containers/" + String.join(",", ids) + "/json";
                    var request = new HttpInspectSpec(url).toRequest();
                    var response = dockerSpec.socket().send(request, r -> HtJson.toMapList(r.reader()));
                    return response.body().stream()
                            .map(HtJsonContainer::new);
                });
    }
}
