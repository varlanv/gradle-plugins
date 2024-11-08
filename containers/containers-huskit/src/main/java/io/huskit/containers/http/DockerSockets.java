package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.Os;
import io.huskit.common.Volatile;

import java.util.concurrent.ScheduledExecutorService;

final class DockerSockets {

    private static final Volatile<DockerSocket> DEFAULT_SOCKET = Volatile.of();

    public DockerSocket pickDefault(ScheduledExecutorService executor, Log log) {
        return DEFAULT_SOCKET.syncSetOrGet(() -> {
            if (Os.WINDOWS.isCurrent()) {
                return new NpipeDocker(HtConstants.NPIPE_SOCKET, executor, log);
            } else {
                throw new IllegalStateException("Unsupported OS");
            }
        });
    }
}
