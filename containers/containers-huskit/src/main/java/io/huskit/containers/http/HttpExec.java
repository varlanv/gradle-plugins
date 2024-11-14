package io.huskit.containers.http;

import io.huskit.common.concurrent.FinishFuture;
import io.huskit.containers.api.container.exec.HtExec;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
final class HttpExec implements HtExec {

    HtHttpDockerSpec dockerSpec;
    HttpExecSpec httpExecSpec;

    @Override
    public void exec() {
        FinishFuture.finish(execAsync(), dockerSpec.defaultTimeout());
    }

    @Override
    public CompletableFuture<Void> execAsync() {
        return dockerSpec.socket().sendPushAsync(
            new PushRequest<>(
                new Request(
                    dockerSpec.requests().post(httpExecSpec)
                ),
                PushResponse.ready()
            )
        ).thenApply(v -> null);
    }
}
