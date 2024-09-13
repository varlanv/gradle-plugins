package io.huskit.containers.api;

import java.util.function.Function;

public interface HtCliDocker extends HtDocker {

    HtCliDocker configure(Function<HtCliDockerSpec, HtCliDockerSpec> configurer);

    void close();
}
