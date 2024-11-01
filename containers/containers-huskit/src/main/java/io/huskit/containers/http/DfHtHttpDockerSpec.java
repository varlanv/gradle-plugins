package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
final class DfHtHttpDockerSpec implements HtHttpDockerSpec {

    DockerSockets sockets;
    @With
    @Getter
    Boolean isCleanOnClose;
    @Getter
    HttpRequests requests;
    Executor executor;

    public DfHtHttpDockerSpec() {
        this(new DockerSockets(), false, new HttpRequests(), Executors.newFixedThreadPool(3));
    }

    @Override
    public DockerSocket socket() {
        return sockets.pickDefault(executor);
    }
}
