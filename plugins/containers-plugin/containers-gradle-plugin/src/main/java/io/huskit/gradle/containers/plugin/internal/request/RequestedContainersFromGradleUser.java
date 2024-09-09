package io.huskit.gradle.containers.plugin.internal.request;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.DefaultRequestedContainer;
import io.huskit.containers.model.image.DefaultContainerImage;
import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.port.DynamicContainerPort;
import io.huskit.containers.model.port.FixedContainerPort;
import io.huskit.containers.model.port.PortRange;
import io.huskit.containers.model.request.DefaultMongoRequestedContainer;
import io.huskit.containers.model.request.MongoExposedEnvironment;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.reuse.ContainerCleanupOptions;
import io.huskit.containers.model.reuse.DefaultMongoContainerReuseOptions;
import io.huskit.gradle.containers.plugin.api.ContainerPortSpec;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoContainerRequestSpec;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RequestedContainersFromGradleUser implements RequestedContainers {

    Collection<ContainerRequestSpecView> containersRequestedByUser;

    @Override
    public Stream<RequestedContainer> stream() {
        return containersRequestedByUser.stream()
                .map(ContainerRequestSpec.class::cast)
                .map(requested -> {
                    var containerType = requested.containerType();
                    if (containerType == ContainerType.MONGO) {
                        var mongoRequested = (MongoContainerRequestSpec) requested;
                        var containerReuseSpec = mongoRequested.getReuse().get();
                        var exposedEnvironmentSpec = mongoRequested.getExposedEnvironment().get();
                        var cleanupSpec = containerReuseSpec.getCleanupSpec().get();
                        var port = requested.getPort().get();
                        return new DefaultMongoRequestedContainer(
                                new DefaultRequestedContainer(
                                        () -> requested.getProjectPath().get(),
                                        new DefaultContainerImage(requested.getImage().get()),
                                        requested.id(),
                                        getPort(port),
                                        containerType,
                                        new DefaultMongoContainerReuseOptions(
                                                Boolean.TRUE.equals(containerReuseSpec.getEnabled().getOrNull()),
                                                Boolean.TRUE.equals(containerReuseSpec.getNewDatabaseForEachTask().getOrNull()),
                                                Boolean.TRUE.equals(containerReuseSpec.getReuseBetweenBuilds().getOrNull()),
                                                ContainerCleanupOptions.after(cleanupSpec.getCleanupAfter().getOrNull())
                                        )
                                ),
                                new MongoExposedEnvironment.Default(
                                        exposedEnvironmentSpec.getConnectionString().get(),
                                        exposedEnvironmentSpec.getDatabaseName().get(),
                                        exposedEnvironmentSpec.getPort().get()
                                ),
                                mongoRequested.getDatabaseName().get()
                        );
                    } else {
                        throw new UnsupportedOperationException("Container type [" + containerType + "] is not supported");
                    }
                });
    }

    private ContainerPort getPort(ContainerPortSpec portSpec) {
        return Optional.ofNullable(portSpec.getFixed().getOrNull())
                .map(fixedPort -> {
                    var hostValue = fixedPort.getHostValue().getOrNull();
                    var hostRange = fixedPort.getHostRange().getOrNull();
                    if (hostValue != null) {
                        return new FixedContainerPort(hostValue, fixedPort.getContainerValue().get());
                    } else if (hostRange != null) {
                        return new PortRange(hostRange.left(), hostRange.right(), fixedPort.getContainerValue().get());
                    } else {
                        return null;
                    }
                })
                .orElseGet(DynamicContainerPort::new);
    }

    @Override
    public int size() {
        return containersRequestedByUser.size();
    }
}
