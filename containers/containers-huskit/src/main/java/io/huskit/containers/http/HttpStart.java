package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtLazyContainer;
import io.huskit.containers.api.container.HtStart;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpStart implements HtStart {

    HtHttpDockerSpec dockerSpec;
    HttpStartSpec httpStartSpec;
    String containerId;

    @Override
    public HtContainer exec() {
        var response = dockerSpec.socket().send(httpStartSpec.toRequest(containerId), r -> HtJson.toMapList(r.reader()));
        if (response.head().status() != 204) {
            throw new RuntimeException(String.format("Failed to start container, received status code %s - %s",
                    response.head().status(), response.body().list()));
        }
        return new HtLazyContainer(
                containerId,
                () -> new HttpInspect(dockerSpec).inspect(containerId)
        );
    }
}
