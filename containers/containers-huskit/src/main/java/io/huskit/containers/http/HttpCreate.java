package io.huskit.containers.http;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtCreate;
import io.huskit.containers.api.container.HtLazyContainer;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.internal.HtJson;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HttpCreate implements HtCreate {

    HtHttpDockerSpec dockerSpec;
    HttpCreateSpec httpCreateSpec;
    HttpInspect httpInspect;
    HtImgName image;

    @Override
    public HtContainer exec() {
        var response = dockerSpec.socket().send(httpCreateSpec.toRequest(image));
        var status = response.head().status();
        if (status != 201) {
            throw new RuntimeException(String.format("Failed to create container, received status %s - %s", status, response.body().list()));
        }
        var id = (String) HtJson.toMap(response.body().singleLine()).get("Id");
        return new HtLazyContainer(id, () -> httpInspect.inspect(id));
    }
}
