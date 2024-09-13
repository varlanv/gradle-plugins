package io.huskit.containers.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.List;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class CliCommand implements HtCommand {

    List<String> value;
    @With
    Predicate<String> terminatePredicate;
    @With
    Predicate<String> linePredicate;

    public CliCommand(List<String> value) {
        this(value, (line) -> false, (line) -> true);
    }
}
