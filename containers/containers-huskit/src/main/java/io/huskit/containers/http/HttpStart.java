package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtLazyContainer;
import io.huskit.containers.api.container.HtStart;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class HttpStart implements HtStart {

    HtHttpDockerSpec dockerSpec;
    HttpStartSpec httpStartSpec;
    String containerId;

    @Override
    public HtContainer exec() {
        dockerSpec.socket().send(
                new Request(
                        httpStartSpec.toRequest(containerId)
                ).withExpectedStatus(204)
        );
        return new HtLazyContainer(
                containerId,
                () -> new HttpInspect(dockerSpec).inspect(containerId)
        );
    }
}
