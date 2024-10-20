package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtCreate;
import io.huskit.containers.api.container.HtLazyContainer;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
final class HttpCreate implements HtCreate {

    HtHttpDockerSpec dockerSpec;
    HttpCreateSpec httpCreateSpec;
    HttpInspect httpInspect;

    @Override
    public HtContainer exec() {
        return execAsync().join();
    }

    @Override
    public CompletableFuture<HtContainer> execAsync() {
        return dockerSpec.socket()
                .sendAsync(
                        new Request(
                                dockerSpec.requests().post(httpCreateSpec)
                        ).withExpectedStatus(201)
                )
                .thenApply(response -> {
                    var id = (String) HtJson.toMap(response.bodyReader().orElseThrow()).get("Id");
                    return new HtLazyContainer(id, () -> httpInspect.inspect(id));
                });
    }
}
