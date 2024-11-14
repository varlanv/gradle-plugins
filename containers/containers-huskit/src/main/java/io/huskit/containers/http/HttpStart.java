package io.huskit.containers.http;

import io.huskit.common.concurrent.FinishFuture;
import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtLazyContainer;
import io.huskit.containers.api.container.HtStart;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
final class HttpStart implements HtStart {

    HtHttpDockerSpec dockerSpec;
    HttpStartSpec httpStartSpec;
    String containerId;

    @Override
    @SneakyThrows
    public HtContainer exec() {
        return FinishFuture.finish(execAsync(), dockerSpec.defaultTimeout());
    }

    @Override
    public CompletableFuture<HtContainer> execAsync() {
        return dockerSpec.socket().sendPushAsync(
            new PushRequest<>(
                new Request(
                    httpStartSpec.toRequest(containerId)
                ).withExpectedStatus(204),
                PushResponse.ready()
            )
        ).thenApply(
            r -> new HtLazyContainer(
                containerId,
                () -> new HttpInspect(dockerSpec).inspect(containerId)
            )
        );
    }
}
