package io.huskit.containers.model.request;

import io.huskit.containers.model.id.ContainerKey;
import io.huskit.containers.model.image.ContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.reuse.MongoContainerReuseOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class DefaultMongoRequestedContainer implements MongoRequestedContainer {

    ContainerRequestSource source;
    MongoExposedEnvironment exposedEnvironment;
    String databaseName;
    ContainerImage image;
    ContainerPort port;
    ContainerKey key;
    MongoContainerReuseOptions reuseOptions;
}
