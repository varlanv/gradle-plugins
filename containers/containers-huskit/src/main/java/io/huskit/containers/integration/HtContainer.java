package io.huskit.containers.integration;

import java.util.function.Consumer;

public interface HtContainer {

    HtContainer withReuse();

    HtContainer withDockerClientSpec(Consumer<DockerClientSpec> dockerClientSpecAction);

    HtContainer withContainerSpec(Consumer<ContainerSpec> containerSpecAction);

    HtStartedContainer start();
}
