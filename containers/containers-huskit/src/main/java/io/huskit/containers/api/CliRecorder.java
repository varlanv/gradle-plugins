package io.huskit.containers.api;

import io.huskit.containers.cli.HtCommand;

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
