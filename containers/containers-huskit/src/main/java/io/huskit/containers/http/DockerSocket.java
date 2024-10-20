package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.common.function.ThrowingFunction;
import io.huskit.containers.api.container.logs.LookFor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

interface DockerSocket {

    <T> Http.Response<T> send(Request<T> request);

    <T> CompletableFuture<Http.Response<T>> sendAsync(Request<T> request);

    void release();

    default CloseableDockerSocket closeable() {
        return new DfCloseableDockerSocket(this);
    }

    interface CloseableDockerSocket extends DockerSocket, AutoCloseable {

        @Override
        default void close() throws Exception {
            release();
        }
    }
}

@RequiredArgsConstructor
final class DfCloseableDockerSocket implements DockerSocket.CloseableDockerSocket {

    DockerSocket delegate;

    @Override
    public <T> Http.Response<T> send(Request<T> request) {
        return delegate.send(request);
    }

    @Override
    public <T> CompletableFuture<Http.Response<T>> sendAsync(Request<T> request) {
        return delegate.sendAsync(request);
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

final class Request<T> {

    @Getter
    Http.Request http;
    @Getter
    ThrowingFunction<Npipe.HttpFlow, T> action;
    Mutable<RepeatRead> repeatReadPredicate;
    Mutable<ExpectedStatus> expectedStatus;

    public Request(Http.Request http,
                   ThrowingFunction<Npipe.HttpFlow, T> action) {
        this.http = http;
        this.action = action;
        this.repeatReadPredicate = Mutable.of();
        this.expectedStatus = Mutable.of();
    }

    public Request<T> withRepeatReadPredicate(LookFor lookFor, Duration backoff) {
        repeatReadPredicate.set(new RepeatRead(lookFor, backoff));
        return this;
    }

    public Request<T> withExpectedStatus(Integer status) {
        expectedStatus.set(new ExpectedStatus(status));
        return this;
    }

    public Optional<RepeatRead> repeatReadPredicate() {
        return repeatReadPredicate.maybe();
    }

    public Optional<ExpectedStatus> expectedStatus() {
        return expectedStatus.maybe();
    }
}

final class RawRequest {

    @Getter
    Http.Request http;
    Mutable<RepeatRead> repeatReadPredicate;
    Mutable<ExpectedStatus> expectedStatus;

    public RawRequest(Http.Request http) {
        this.http = http;
        this.repeatReadPredicate = Mutable.of();
        this.expectedStatus = Mutable.of();
    }

    public RawRequest withRepeatReadPredicate(LookFor lookFor, Duration backoff) {
        repeatReadPredicate.set(new RepeatRead(lookFor, backoff));
        return this;
    }

    public RawRequest withExpectedStatus(Integer status) {
        expectedStatus.set(new ExpectedStatus(status));
        return this;
    }

    public Optional<RepeatRead> repeatReadPredicate() {
        return repeatReadPredicate.maybe();
    }

    public Optional<ExpectedStatus> expectedStatus() {
        return expectedStatus.maybe();
    }
}

@Getter
@RequiredArgsConstructor
final class ExpectedStatus {

    Integer status;
}

@Getter
@RequiredArgsConstructor
final class RepeatRead {

    LookFor lookFor;
    Duration backoff;
}
