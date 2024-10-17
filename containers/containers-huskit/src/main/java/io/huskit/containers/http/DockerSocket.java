package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.common.function.ThrowingFunction;
import io.huskit.containers.api.container.logs.LookFor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface DockerSocket {

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

    @RequiredArgsConstructor
    class DfCloseableDockerSocket implements CloseableDockerSocket {

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

    class Request<T> {

        @Getter
        Http.Request http;
        @Getter
        ThrowingFunction<Npipe.HttpFlow, List<T>> action;
        Mutable<RepeatRead> repeatReadPredicate;
        Mutable<ExpectedStatus> expectedStatus;

        public Request(Http.Request http,
                       ThrowingFunction<Npipe.HttpFlow, List<T>> action) {
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

        @Getter
        @RequiredArgsConstructor
        public static class ExpectedStatus {

            Integer status;
        }

        @Getter
        @RequiredArgsConstructor
        public static class RepeatRead {

            LookFor lookFor;
            Duration backoff;
        }
    }
}