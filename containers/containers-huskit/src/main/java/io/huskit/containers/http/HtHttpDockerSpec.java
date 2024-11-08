package io.huskit.containers.http;

import io.huskit.common.Log;

public interface HtHttpDockerSpec {

    DockerSocket socket();

    Boolean isCleanOnClose();

    HttpRequests requests();

    Log log();
}
