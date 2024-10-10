package io.huskit.containers.core;

import io.huskit.containers.integration.HtIntegratedDocker;
import io.huskit.containers.integration.HtStartedContainer;
import io.huskit.containers.model.ContainersRequest;
import io.huskit.log.Log;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ContainersApplication implements AutoCloseable {

    Log log;
    HtIntegratedDocker integratedDocker;

    public static ContainersApplication application(Log commonLog, HtIntegratedDocker integratedDocker) {
        return new ContainersApplication(
                commonLog,
                integratedDocker
        );
    }

    public Map<String, HtStartedContainer> containers(ContainersRequest request) {
        return integratedDocker.feed(request.requestedContainers());
    }

    @Override
    public void close() throws IOException {
        integratedDocker.stop();
    }
}
