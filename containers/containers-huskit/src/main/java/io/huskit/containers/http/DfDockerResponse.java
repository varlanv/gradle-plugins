package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class DfDockerResponse implements DockerResponse {

    Head head;
    Body body;
}
