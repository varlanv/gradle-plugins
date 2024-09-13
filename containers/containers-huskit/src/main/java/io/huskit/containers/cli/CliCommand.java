package io.huskit.containers.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class CliCommand implements HtCommand {

    CommandType type;
    List<String> value;
    @With
    Predicate<String> terminatePredicate;
    @With
    Predicate<String> linePredicate;
    @With
    Duration timeout;

    public CliCommand(CommandType type, List<String> value) {
        this(type, value, (line) -> false, (line) -> true, Duration.ZERO);
    }
}
