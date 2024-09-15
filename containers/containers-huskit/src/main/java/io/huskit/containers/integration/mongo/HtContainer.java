package io.huskit.containers.integration.mongo;

import java.util.function.Consumer;

public interface HtContainer {

    HtContainer withContainerSpec(Consumer<ContainerSpec> containerSpecAction);
}
