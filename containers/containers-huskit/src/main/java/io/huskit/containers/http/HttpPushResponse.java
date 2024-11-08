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
    Mutable<Http.Response<T>> response = Mutable.of();

    @Override
    public boolean isReady() {
        return response.isPresent();
    }

    @Override
    public Http.Response<T> value() {
        return response.require();
    }

    @Override
    public Optional<Http.Response<T>> apply(ByteBuffer byteBuffer) {
        if (futureHead.isReady()) {
            return getHttpResponse(byteBuffer, futureHead.value());
        } else {
            var maybeHead = futureHead.apply(byteBuffer);
            if (maybeHead.isEmpty()) {
                return Optional.empty();
            } else {
                return getHttpResponse(byteBuffer, maybeHead.get());
            }
        }
    }

    private Optional<Http.Response<T>> getHttpResponse(ByteBuffer byteBuffer, Http.Head head) {
        var maybeBody = request.pushResponse().apply(byteBuffer);
        if (maybeBody.isEmpty()) {
            return Optional.empty();
        } else {
            var value = Http.Response.of(head, Http.Body.of(maybeBody.get()));
            response.set(value);
            if (request.request().expectedStatus().isPresent()) {
                if (!Objects.equals(head.status(), request.request().expectedStatus().get().status())) {
                    throw new IllegalStateException(
                        "Expected HTTP status "
                            + request.request().expectedStatus().get().status()
                            + " but got "
                            + head.status()
                            + ": " + value.body().value()
                    );
                }
            }
            return Optional.of(value);
        }
    }
}