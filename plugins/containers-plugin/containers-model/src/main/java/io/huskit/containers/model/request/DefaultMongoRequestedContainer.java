package io.huskit.containers.model.request;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.reuse.MongoContainerReuseOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DefaultMongoRequestedContainer implements MongoRequestedContainer {

    RequestedContainer requestedContainer;
    @Getter
    MongoExposedEnvironment exposedEnvironment;
    @Getter
    String databaseName;

    @Override
    public ContainerRequestSource source() {
        return requestedContainer.source();
    }

    @Override
    public ContainerImage image() {
        return requestedContainer.image();
    }

    @Override
    public ContainerPort port() {
        return requestedContainer.port();
    }

    @Override
    public ContainerId id() {
        return requestedContainer.id();
    }

    @Override
    public ContainerType containerType() {
        return requestedContainer.containerType();
    }

    @Override
    public MongoContainerReuseOptions reuseOptions() {
        return (MongoContainerReuseOptions) requestedContainer.reuseOptions();
    }
}
