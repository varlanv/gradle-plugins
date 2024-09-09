package io.huskit.gradle.containers.plugin.internal.request;

import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RequestedContainersFromGradleUser implements RequestedContainers {

    Collection<ContainerRequestSpec> containersRequestedByUser;

    @Override
    public Stream<RequestedContainer> stream() {
        return containersRequestedByUser.stream()
                .map(ContainerRequestSpec::toRequestedContainer);
    }

    @Override
    public int size() {
        return containersRequestedByUser.size();
    }
}
