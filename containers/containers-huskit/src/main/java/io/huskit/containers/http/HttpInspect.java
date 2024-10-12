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
        var response = dockerSpec.socket().send(new HttpInspectSpec(id).toRequest());
        var status = response.head().status();
        if (status != 200) {
            throw new RuntimeException(String.format("Failed to inspect container, received status %s - %s", status, response.body().list()));
        }
        return new HtJsonContainer(HtJson.toMap(response.body().singleLine()));
    }

    public Stream<HtContainer> inspect(Iterable<? extends CharSequence> containerIds) {
        return Stream.of(containerIds)
                .flatMap(ids -> {
                    var url = "/containers/" + String.join(",", ids) + "/json";
                    var request = new HttpInspectSpec(url).toRequest();
                    var response = dockerSpec.socket().send(request);
                    return response.body().stream()
                            .flatMap(HtJson::toMapStream)
                            .map(HtJsonContainer::new);
                });
    }
}
