package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class DfDockerRequest implements DockerRequest {

    byte[] body;
}
