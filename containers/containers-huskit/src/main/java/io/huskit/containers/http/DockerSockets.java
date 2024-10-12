package io.huskit.containers.http;

import io.huskit.common.Os;
import io.huskit.common.Volatile;

public class DockerSockets {

    private static final Volatile<DockerSocket> DEFAULT_SOCKET = Volatile.of();

    public DockerSocket pickDefault() {
        return DEFAULT_SOCKET.syncSetOrGet(() -> {
            if (Os.WINDOWS.isCurrent()) {
                return new DockerNpipe();
            } else {
                throw new IllegalStateException("Unsupported OS");
            }
        });
    }
}
