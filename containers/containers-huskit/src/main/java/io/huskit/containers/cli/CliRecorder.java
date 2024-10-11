package io.huskit.containers.cli;

import java.util.Collection;

public interface CliRecorder {

    void record(HtCommand command);

    static CliRecorder noop() {
        return command -> {
        };
    }

    static CliRecorder collection(Collection<HtCommand> collection) {
        return collection::add;
    }
}
