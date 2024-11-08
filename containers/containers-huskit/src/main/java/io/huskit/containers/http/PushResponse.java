package io.huskit.containers.http;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

interface PushResponse<T> {

    boolean isReady();

    T value();

    Optional<T> apply(ByteBuffer byteBuffer);

    static PushResponse<?> ready() {
        return new PushResponse<>() {

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public Object value() {
                return true;
            }

            @Override
            public Optional<Object> apply(ByteBuffer byteBuffer) {
                return Optional.of(true);
            }
        };
    }

    static <T> PushResponse<T> fake(Function<ByteBuffer, Optional<T>> action) {
        return new PushResponse<>() {

            @Override
            public boolean isReady() {
                throw new UnsupportedOperationException();
            }

            @Override
            public T value() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<T> apply(ByteBuffer byteBuffer) {
                return action.apply(byteBuffer);
            }
        };
    }
}