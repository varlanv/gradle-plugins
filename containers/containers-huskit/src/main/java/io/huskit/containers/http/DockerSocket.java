package io.huskit.containers.http;

import java.util.concurrent.CompletableFuture;

public interface DockerSocket {

    CompletableFuture<DockerResponse> sendAsync(DockerRequest request);

    DockerResponse send(DockerRequest request);

    void close();
}
