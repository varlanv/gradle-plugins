package io.huskit.gradle.containers.plugin.internal;

import io.huskit.gradle.containers.plugin.api.ContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkParameters;

public abstract class ContainerWorkActionParameters implements WorkParameters {

    @Internal
    public abstract Property<ContainersBuildService> getContainers();

    @Input
    public abstract ListProperty<ContainerRequestSpec> getRequestedContainers();
}
