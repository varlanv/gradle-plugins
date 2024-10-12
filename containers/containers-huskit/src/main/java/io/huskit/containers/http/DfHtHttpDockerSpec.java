package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

@RequiredArgsConstructor
public class DfHtHttpDockerSpec implements HtHttpDockerSpec {

    DockerSockets sockets;
    @With
    @Getter
    Boolean isCleanOnClose;

    public DfHtHttpDockerSpec() {
        this(new DockerSockets(), false);
    }

    @Override
    public DockerSocket socket() {
        return sockets.pickDefault();
    }
}
