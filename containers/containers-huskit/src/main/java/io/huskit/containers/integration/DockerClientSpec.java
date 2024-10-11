package io.huskit.containers.integration;

import io.huskit.containers.api.docker.HtDocker;

public interface DockerClientSpec {

    HtServiceContainer withDocker(HtDocker docker);
}
