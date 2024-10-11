package io.huskit.containers.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public class CommandResult {

    List<String> lines;

    public String singleLine() {
        if (lines.size() != 1) {
            throw new IllegalStateException("Expected a single line, but got " + lines.size() + " lines");
        }
        return lines.get(0);
    }

    public Stream<String> stream() {
        return lines.stream();
    }
}
