package io.huskit.containers.cli;

import io.huskit.containers.model.CommandType;
import io.huskit.common.HtConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
class CliCommand implements HtCommand {

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
                HtConstants.Predicates.alwaysFalse(),
                HtConstants.Predicates.alwaysTrue(),
                Duration.ZERO
        );
    }

    public CliCommand(CommandType type, Set<String> value) {
        this(
                type,
                List.copyOf(value),
                HtConstants.Predicates.alwaysFalse(),
                HtConstants.Predicates.alwaysTrue(),
                Duration.ZERO
        );
    }
}
