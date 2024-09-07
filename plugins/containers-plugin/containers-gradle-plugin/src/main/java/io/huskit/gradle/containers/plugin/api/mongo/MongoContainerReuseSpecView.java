package io.huskit.gradle.containers.plugin.api.mongo;

import io.huskit.gradle.containers.plugin.api.ContainerReuseSpecView;

public interface MongoContainerReuseSpecView extends ContainerReuseSpecView {

    void newDatabaseForEachTask(boolean newDatabaseForEachTask);
}
