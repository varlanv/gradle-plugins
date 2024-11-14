package io.huskit.containers.http;

import io.huskit.common.Mutable;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
final class HttpPushResponse<T> implements PushResponse<Http.Response<T>> {

    PushResponse<Http.Head> futureHead;
    PushRequest<T> request;
    Mutable<PushResponse<T>> pushResponse;
    Mutable<Http.Response<T>> response;

    HttpPushResponse(PushResponse<Http.Head> futureHead, PushRequest<T> request) {
        this.futureHead = Objects.requireNonNull(futureHead);
        this.request = Objects.requireNonNull(request);
        this.pushResponse = Mutable.of(request.pushResponse());
        this.response = Mutable.of();
    }

    @Override
    public Optional<Http.Response<T>> value() {
        return response.maybe();
    }

    @Override
    public Optional<Http.Response<T>> push(ByteBuffer byteBuffer) {
        return futureHead.value()
                         .flatMap(val -> getHttpResponse(byteBuffer, val))
                         .or(
                             () -> futureHead.push(byteBuffer)
                                             .flatMap(
                                                 head -> {
                                                     if (head.isChunked() && !(pushResponse.require() instanceof PushMultiplexedStream)) {
                                                         pushResponse.set(new PushChunked<>(pushResponse.require()));
                                                     }
                                                     return getHttpResponse(byteBuffer, head);
                                                 }
                                             )
                         );
    }

    private Optional<Http.Response<T>> getHttpResponse(ByteBuffer byteBuffer, Http.Head head) {
        if (request.request().expectedStatus().isPresent()) {
            if (!Objects.equals(head.status(), request.request().expectedStatus().get().status())) {
                throw new IllegalStateException(
                    "Expected HTTP status "
                        + request.request().expectedStatus().get().status()
                        + " but got "
                        + head.status()
                        + new PushRaw().push(byteBuffer).map(body -> ": " + body).orElse("")
                );
            }
        }
        var maybeBody = pushResponse.require().push(byteBuffer);
        if (maybeBody.isEmpty()) {
            return Optional.empty();
        } else {
            var value = Http.Response.of(head, Http.Body.of(maybeBody.get()));
            response.set(value);
            return Optional.of(value);
        }
    }
}