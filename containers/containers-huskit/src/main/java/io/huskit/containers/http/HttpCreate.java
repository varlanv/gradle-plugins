package io.huskit.containers.http;

import io.huskit.common.concurrent.FinishFuture;
import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtCreate;
import io.huskit.containers.api.container.HtLazyContainer;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
final class HttpCreate implements HtCreate {

    HtHttpDockerSpec dockerSpec;
    HttpCreateSpec httpCreateSpec;
    HttpInspect httpInspect;

    @Override
    public HtContainer exec() {
        return FinishFuture.finish(execAsync(), dockerSpec.defaultTimeout());
    }

    @Override
    public CompletableFuture<HtContainer> execAsync() {
        return dockerSpec.socket()
            .sendPushAsync(
                new PushRequest<>(
                    new Request(
                        dockerSpec.requests().post(httpCreateSpec)
                    ).withExpectedStatus(201),
                    new PushJsonObject()
                )
            )
            .thenApply(
                response -> {
                    var id = (String) response.body().value().get("Id");
                    return new HtLazyContainer(
                        id,
                        () -> httpInspect.inspect(id)
                    );
                }
            );
    }
}
