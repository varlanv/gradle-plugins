package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.provider.Property;

public interface ContainerReuseSpec {

    Property<Boolean> getNewDatabaseForEachTask();

    Property<Boolean> getReuseBetweenBuilds();
}
