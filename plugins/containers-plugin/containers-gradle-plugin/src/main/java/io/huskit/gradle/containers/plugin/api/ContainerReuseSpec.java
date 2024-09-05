package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.provider.Property;

/**
 * Configuration for reusing a container.
 */
public interface ContainerReuseSpec {

    Property<Boolean> getEnabled();

    Property<Boolean> getReuseBetweenBuilds();
}
