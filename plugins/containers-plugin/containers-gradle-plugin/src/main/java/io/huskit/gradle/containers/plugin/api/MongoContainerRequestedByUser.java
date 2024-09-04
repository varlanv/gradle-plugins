package io.huskit.gradle.containers.plugin.api;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.id.MongoContainerId;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

public interface MongoContainerRequestedByUser extends ContainerRequestedByUserForTask {

    Property<String> getDatabaseName();

    Property<MongoContainerReuseRequestedByUser> getReuse();

    default ContainerId id() {
        var reuse = getReuse().get();
        return new MongoContainerId(
                getRootProjectName().get(),
                getProjectName().get(),
                getImage().get(),
                getDatabaseName().get(),
                reuse.getReuseBetweenBuilds().get(),
                reuse.getNewDatabaseForEachTask().get(),
                reuse.getEnabled().get()
        );
    }

    default void reuse(Action<MongoContainerReuseRequestedByUser> action) {
        var reuse = getReuse().get();
        reuse.getEnabled().set(true);
        action.execute(reuse);
    }

    default void reuse() {
        var reuse = getReuse().get();
        reuse.getEnabled().set(true);
    }

    @Override
    default ContainerType containerType() {
        return ContainerType.MONGO;
    }
}
