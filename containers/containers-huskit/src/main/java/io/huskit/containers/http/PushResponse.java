package io.huskit.containers.http;

import io.huskit.common.Mutable;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;

interface PushResponse<T> {

    Optional<T> value();

    Optional<T> push(ByteBuffer byteBuffer);

    static PushResponse<?> ready() {
        return new PushResponse<>() {

            @Override
            public Optional<Object> value() {
                return Optional.of(true);
            }

            @Override
            public Optional<Object> push(ByteBuffer byteBuffer) {
                return Optional.of(true);
            }
        };
    }

    static <T> PushResponse<T> fake(Function<ByteBuffer, Optional<T>> action) {
        return new PushResponse<>() {

            Mutable<T> value = Mutable.of();

            @Override
            public Optional<T> value() {
                return value.maybe();
            }

            @Override
            public Optional<T> push(ByteBuffer byteBuffer) {
                return value.maybe()
                            .or(
                                () -> {
                                    var val = action.apply(byteBuffer);
                                    val.ifPresent(value::set);
                                    return val;
                                }
                            );
            }
        };
    }
}
