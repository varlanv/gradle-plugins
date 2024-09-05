package io.huskit.gradle.containers.plugin.api.mongo;

import io.huskit.gradle.containers.plugin.api.ContainerReuseSpec;
import org.gradle.api.provider.Property;

public interface MongoContainerReuseSpec extends ContainerReuseSpec {

    Property<Boolean> getNewDatabaseForEachTask();
}
