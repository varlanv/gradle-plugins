package io.huskit.containers.api.container.logs;

import java.time.Duration;
import java.util.function.Predicate;

class LookForNothing implements LookFor {

    static final LookForNothing INSTANCE = new LookForNothing();

    @Override
    public LookFor onlyInStdOut() {
        return this;
    }

    @Override
    public LookFor onlyInStdErr() {
        return this;
    }

    @Override
    public Boolean isOnlyInStdOut() {
        return false;
    }

    @Override
    public Boolean isOnlyInStdErr() {
        return false;
    }

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
