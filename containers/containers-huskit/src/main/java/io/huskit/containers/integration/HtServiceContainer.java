package io.huskit.containers.integration;

import java.util.function.Consumer;

public interface HtServiceContainer {

    HtServiceContainer withDockerClientSpec(Consumer<DockerClientSpec> dockerClientSpecAction);

    HtServiceContainer withContainerSpec(Consumer<ContainerSpec> containerSpecAction);

    HtStartedContainer start();
}
