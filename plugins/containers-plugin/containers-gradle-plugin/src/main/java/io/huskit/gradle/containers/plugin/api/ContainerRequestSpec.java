package io.huskit.gradle.containers.plugin.api;

import io.huskit.containers.model.ContainerType;
import org.gradle.api.provider.Property;

public interface ContainerRequestSpec {

    Property<String> getImage();

    Property<Integer> getFixedPort();

    ContainerType containerType();
}
