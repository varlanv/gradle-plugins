package io.huskit.containers.api.cli;

import io.huskit.containers.api.HtDocker;

import java.util.function.Consumer;

public interface HtCliDocker extends HtDocker {

    @Override
    HtCliDocker withCleanOnClose(Boolean cleanOnClose);

    HtCliDocker configure(Consumer<HtCliDockerSpec> configureAction);

    void close();
}
