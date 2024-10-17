package io.huskit.containers.http;

import io.huskit.common.function.ThrowingFunction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DockerSocket {

    <T> CompletableFuture<Http.Response<T>> sendAsync(Http.Request request, ThrowingFunction<Npipe.HttpFlow, List<T>> action);

    <T> Http.Response<T> send(Http.Request request, ThrowingFunction<Npipe.HttpFlow, List<T>> action);

    void close();
}
