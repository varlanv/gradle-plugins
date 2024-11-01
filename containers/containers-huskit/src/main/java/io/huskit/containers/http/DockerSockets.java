package io.huskit.containers.http;

import io.huskit.common.HtConstants;
import io.huskit.common.Os;
import io.huskit.common.Volatile;

import java.util.concurrent.Executor;

final class DockerSockets {

    private static final Volatile<DockerSocket> DEFAULT_SOCKET = Volatile.of();

    public DockerSocket pickDefault(Executor executor) {
        return DEFAULT_SOCKET.syncSetOrGet(() -> {
            if (Os.WINDOWS.isCurrent()) {
                return new NpipeDocker(HtConstants.NPIPE_SOCKET, executor);
            } else {
                throw new IllegalStateException("Unsupported OS");
            }
        });
    }
}
