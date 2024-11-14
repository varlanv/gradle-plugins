package io.huskit.containers.http;

import io.huskit.common.concurrent.FinishFuture;
import io.huskit.containers.api.container.rm.HtRm;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class HttpRm implements HtRm {

    HtHttpDockerSpec dockerSpec;
    HttpRmSpec spec;
    Iterable<? extends CharSequence> containerIds;

    @Override
    public void exec() {
        var ran = false;
        for (var containerId : containerIds) {
            FinishFuture.finish(
                dockerSpec.socket().sendPushAsync(
                    new PushRequest<>(
                        new Request(
                            spec.toRequest(containerId)
                        ).withExpectedStatus(204),
                        PushResponse.ready()
                    )
                ),
                dockerSpec.defaultTimeout()
            );
            ran = true;
        }
        if (!ran) {
            throw new IllegalStateException("Received empty container ID list for removal");
        }
    }
}
