package io.huskit.containers.integration;

import io.huskit.containers.api.HtDocker;

public interface DockerClientSpec {

    HtServiceContainer withDocker(HtDocker docker);
}
