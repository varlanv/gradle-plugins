package io.huskit.containers.integration;

import io.huskit.containers.model.ContainerType;

import java.util.function.Consumer;

public interface HtServiceContainer {

    HtServiceContainer withDockerClientSpec(Consumer<DockerClientSpec> dockerClientSpecAction);

    HtServiceContainer withContainerSpec(Consumer<ContainerSpec> containerSpecAction);

    HtStartedContainer start();

    ContainerType containerType();
}
