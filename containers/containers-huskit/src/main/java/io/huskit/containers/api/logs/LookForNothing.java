package io.huskit.containers.api.logs;

import java.time.Duration;
import java.util.function.Predicate;

class LookForNothing implements LookFor {

    static final LookForNothing INSTANCE = new LookForNothing();

    @Override
    public LookFor withTimeout(Duration timeout) {
        return this;
    }

    @Override
    public Duration timeout() {
        return Duration.ZERO;
    }

    @Override
    public Predicate<String> predicate() {
        return line -> false;
    }
}
