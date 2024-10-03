package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.HtDocker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefDockerClientSpec implements DockerClientSpec {

    HtServiceContainer parent;
    @Getter
    Mutable<HtDocker> docker = Volatile.of();

    @Override
    public HtServiceContainer withDocker(HtDocker docker) {
        this.docker.set(docker);
        return parent;
    }
}
