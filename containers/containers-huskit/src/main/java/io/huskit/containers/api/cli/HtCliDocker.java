package io.huskit.containers.api.cli;

import io.huskit.containers.api.HtDocker;

import java.util.function.Consumer;

public interface HtCliDocker extends HtDocker {

    HtCliDocker configure(Consumer<HtCliDockerSpec> configurer);

    void close();
}
