package io.huskit.containers.api.container.logs;

import java.time.Duration;
import java.util.function.Predicate;

public interface LookFor {

    static LookFor nothing() {
        return LookForNothing.INSTANCE;
    }

    static LookFor word(String word) {
        return new PredicatedLookFor(line -> line.contains(word), Duration.ZERO);
    }

    static LookFor lineMatching(Predicate<String> predicate) {
        return new PredicatedLookFor(predicate, Duration.ZERO);
    }

    LookFor withTimeout(Duration timeout);

    Duration timeout();

    Predicate<String> predicate();
}
