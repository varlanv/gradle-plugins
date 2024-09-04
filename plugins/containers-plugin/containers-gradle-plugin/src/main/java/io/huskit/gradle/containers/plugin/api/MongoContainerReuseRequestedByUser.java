package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.provider.Property;

public interface MongoContainerReuseRequestedByUser extends ContainerReuseSpec {

    Property<Boolean> getNewDatabaseForEachTask();
}
