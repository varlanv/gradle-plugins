package io.huskit.containers.cli;

import java.util.List;
import java.util.function.Predicate;

public interface HtCommand {

    CommandType type();

    List<String> value();

    Predicate<String> terminatePredicate();

    Predicate<String> linePredicate();

    HtCommand withTerminatePredicate(Predicate<String> predicate);

    HtCommand withLinePredicate(Predicate<String> predicate);
}
