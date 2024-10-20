package io.huskit.containers.http;

import io.huskit.containers.api.container.exec.HtExec;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
class HttpExec implements HtExec {

    HtHttpDockerSpec dockerSpec;
    HttpExecSpec httpExecSpec;

    @Override
    public void exec() {
        execAsync().join();
    }

    @Override
    public CompletableFuture<Void> execAsync() {
        return dockerSpec.socket().sendAsync(
                new Request<>(
                        dockerSpec.requests().post(httpExecSpec),
                        r -> List.of()
                )
        ).thenApply(v -> null);
    }
}
