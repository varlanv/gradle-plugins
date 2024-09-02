package io.huskit.containers.model.request;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.reuse.MongoContainerReuse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultMongoRequestedContainer implements MongoRequestedContainer {

    RequestedContainer requestedContainer;
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
    public MongoContainerReuse containerReuse() {
        return (MongoContainerReuse) requestedContainer.containerReuse();
    }

    @Override
    public String databaseName() {
        return databaseName;
    }
}
