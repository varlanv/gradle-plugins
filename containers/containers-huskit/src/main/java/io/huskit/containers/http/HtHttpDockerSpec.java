package io.huskit.containers.http;

import io.huskit.common.Log;

import java.time.Duration;

public interface HtHttpDockerSpec {

    DockerSocket socket();

    Boolean isCleanOnClose();

    HttpRequests requests();

    Log log();

    Duration defaultTimeout();
}
