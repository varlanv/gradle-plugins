package io.huskit.containers.api;

import java.util.function.Consumer;

public interface HtCliDocker extends HtDocker {

    HtCliDocker configure(Consumer<HtCliDockerSpec> configurer);

    void close();
}
