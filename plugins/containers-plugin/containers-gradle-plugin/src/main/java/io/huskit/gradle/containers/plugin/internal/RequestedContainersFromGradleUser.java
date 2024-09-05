package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.DefaultRequestedContainer;
import io.huskit.containers.model.image.DefaultContainerImage;
import io.huskit.containers.model.port.FixedContainerPort;
import io.huskit.containers.model.request.DefaultMongoRequestedContainer;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.reuse.DefaultMongoContainerReuseOptions;
import io.huskit.gradle.containers.plugin.api.ContainerRequestForTaskSpec;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpec;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpec;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RequestedContainersFromGradleUser implements RequestedContainers {

    Log log;
    Collection<ContainerRequestSpec> containersRequestedByUser;

    @Override
    public List<RequestedContainer> list() {

        return containersRequestedByUser.stream()
                .map(ContainerRequestForTaskSpec.class::cast)
                .map(requested -> {
                    var containerType = requested.containerType();
                    log.info("Preparing container request with type [{}], id [{}]", containerType, requested.id());
                    if (containerType == ContainerType.MONGO) {
                        var mongoRequested = (MongoContainerRequestSpec) requested;
                        var containerReuseSpec = mongoRequested.getReuse().getOrNull();
                        return new DefaultMongoRequestedContainer(
                                new DefaultRequestedContainer(
                                        () -> requested.getProjectPath().get(),
                                        new DefaultContainerImage(requested.getImage().get()),
                                        requested.id(),
                                        Optional.ofNullable(requested.getFixedPort().getOrNull()).map(FixedContainerPort::new).orElse(null),
                                        containerType,
                                        new DefaultMongoContainerReuseOptions(
                                                containerReuseSpec != null && Boolean.TRUE.equals(containerReuseSpec.getEnabled().getOrNull()),
                                                containerReuseSpec != null && Boolean.TRUE.equals(containerReuseSpec.getNewDatabaseForEachTask().getOrNull()),
                                                containerReuseSpec != null && Boolean.TRUE.equals(containerReuseSpec.getReuseBetweenBuilds().getOrNull())
                                        )
                                ),
                                mongoRequested.getDatabaseName().get()
                        );
                    } else {
                        throw new UnsupportedOperationException("Container type [" + containerType + "] is not supported");
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public int size() {
        return containersRequestedByUser.size();
    }
}
