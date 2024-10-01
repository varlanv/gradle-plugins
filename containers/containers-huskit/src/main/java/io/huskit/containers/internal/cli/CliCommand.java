package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.cli.HtCommand;
import io.huskit.containers.model.Constants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.List;
import java.util.Set;
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
        this(
                type,
                value,
                Constants.Predicates.alwaysFalse(),
                Constants.Predicates.alwaysTrue(),
                Duration.ZERO
        );
    }

    public CliCommand(CommandType type, Set<String> value) {
        this(
                type,
                List.copyOf(value),
                Constants.Predicates.alwaysFalse(),
                Constants.Predicates.alwaysTrue(),
                Duration.ZERO
        );
    }
}
