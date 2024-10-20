package io.huskit.containers.http;

import io.huskit.common.Os;
import io.huskit.common.Volatile;
import io.huskit.containers.model.HtConstants;

final class DockerSockets {

    private static final Volatile<DockerSocket> DEFAULT_SOCKET = Volatile.of();

    public DockerSocket pickDefault() {
        return DEFAULT_SOCKET.syncSetOrGet(() -> {
            if (Os.WINDOWS.isCurrent()) {
                return new Npipe(HtConstants.NPIPE_SOCKET);
            } else {
                throw new IllegalStateException("Unsupported OS");
            }
        });
    }
}
