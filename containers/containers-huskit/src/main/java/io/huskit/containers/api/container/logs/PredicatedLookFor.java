package io.huskit.containers.api.container.logs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class PredicatedLookFor implements LookFor {

    Predicate<String> predicate;
    Duration timeout;
    Boolean onlyInStdOut;
    Boolean onlyInStdErr;

    public PredicatedLookFor(Predicate<String> predicate, Duration timeout) {
        this.predicate = predicate;
        this.timeout = timeout;
        this.onlyInStdOut = false;
        this.onlyInStdErr = false;
    }

    @Override
    public LookFor onlyInStdOut() {
        if (onlyInStdOut) {
            return this;
        }
        return new PredicatedLookFor(predicate, timeout, true, false);
    }

    @Override
    public LookFor onlyInStdErr() {
        if (onlyInStdErr) {
            return this;
        }
        return new PredicatedLookFor(predicate, timeout, false, true);
    }

    @Override
    public Boolean isOnlyInStdOut() {
        return onlyInStdErr;
    }

    @Override
    public Boolean isOnlyInStdErr() {
        return onlyInStdErr;
    }

    @Override
    public Boolean isInBothStd() {
        return !onlyInStdOut && !onlyInStdErr;
    }

    @Override
    public LookFor withTimeout(Duration timeout) {
        return new PredicatedLookFor(predicate, timeout, onlyInStdOut, onlyInStdErr);
    }
}
