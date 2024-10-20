package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
final class DfBody<T> implements Http.Body<T> {

    T value;

    @Override
    public T value() {
        return value;
    }
}
