package io.huskit.containers.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
class DfBody implements Body {

    List<String> lines;

    @Override
    public List<String> list() {
        return lines;
    }

    @Override
    public Stream<String> stream() {
        return lines.stream();
    }

    @Override
    public String singleLine() {
        if (lines.size() == 1) {
            return lines.get(0);
        } else if (lines.isEmpty()) {
            throw new IllegalStateException("Cannot get single line from empty response");
        } else {
            throw new IllegalStateException("Cannot get single line from response with multiple lines");
        }
    }
}
