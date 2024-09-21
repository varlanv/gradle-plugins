package io.huskit.containers.api.cli;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

public interface HtCommand {

    CommandType type();

    List<String> value();

    Duration timeout();

    Predicate<String> terminatePredicate();

    Predicate<String> linePredicate();

    HtCommand withTerminatePredicate(Predicate<String> predicate);

    HtCommand withLinePredicate(Predicate<String> predicate);

    HtCommand withTimeout(Duration timeout);
}
