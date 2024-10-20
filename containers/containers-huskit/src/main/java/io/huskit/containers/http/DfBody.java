package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
class DfBody<T> implements Http.Body<T> {

    List<T> lines;

    @Override
    public List<T> list() {
        return lines;
    }

    @Override
    public Stream<T> stream() {
        return lines.stream();
    }

    @Override
    public T single() {
        if (lines.size() == 1) {
            return lines.get(0);
        } else if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot get single line from empty response");
        } else {
            throw new IllegalStateException("Cannot get single line from response with multiple lines");
        }
    }
}
