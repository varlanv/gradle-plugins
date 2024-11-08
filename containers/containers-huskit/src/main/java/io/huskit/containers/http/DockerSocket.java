package io.huskit.containers.http;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

interface DockerSocket {

    <T> CompletableFuture<Http.Response<T>> sendPushAsync(PushRequest<T> request);

    void release();

    default CloseableDockerSocket closeable() {
        return new DfCloseableDockerSocket(this);
    }

    interface CloseableDockerSocket extends DockerSocket, AutoCloseable {

        @Override
        default void close() {
            release();
        }
    }
}

@RequiredArgsConstructor
final class DfCloseableDockerSocket implements DockerSocket.CloseableDockerSocket {

    DockerSocket delegate;

    @Override
    public <T> CompletableFuture<Http.Response<T>> sendPushAsync(PushRequest<T> request) {
        return delegate.sendPushAsync(request);
    }

    @Override
    public void release() {
        delegate.release();
    }

    @Override
    public CloseableDockerSocket closeable() {
        return this;
    }
}

final class Request {

    @Getter
    Http.Request http;
    Mutable<Boolean> dirtiesConnection = Mutable.of();
    Mutable<ExpectedStatus> expectedStatus = Mutable.of();

    public Request(Http.Request http) {
        this.http = http;
    }

    public Request(byte[] body) {
        this(new DfHttpRequest(body));
    }

    public Request withExpectedStatus(Integer status) {
        expectedStatus.set(new ExpectedStatus(status));
        return this;
    }

    public Request withDirtiesConnection(Boolean dirtiesConnection) {
        this.dirtiesConnection.set(dirtiesConnection);
        return this;
    }

    public Boolean dirtiesConnection() {
        return dirtiesConnection.or(false);
    }

    public Optional<ExpectedStatus> expectedStatus() {
        return expectedStatus.maybe();
    }
}

@Getter
@RequiredArgsConstructor
final class PushRequest<T> {

    Request request;
    PushResponse<T> pushResponse;

    PushRequest(byte[] body, PushResponse<T> pushResponse) {
        this(new Request(body), pushResponse);
    }
}

@Getter
@RequiredArgsConstructor
final class ExpectedStatus {

    Integer status;
}
