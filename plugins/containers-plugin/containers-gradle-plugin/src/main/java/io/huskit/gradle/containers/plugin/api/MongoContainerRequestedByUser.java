package io.huskit.gradle.containers.plugin.api;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.gradle.containers.plugin.internal.MongoContainerId;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

public interface MongoContainerRequestedByUser extends ContainerRequestedByUserForTask {

    Property<String> getDatabaseName();

    Property<MongoContainerReuseRequestedByUser> getReuse();

    default ContainerId id() {
        var reuse = getReuse().get();
        return new MongoContainerId(
                getRootProjectName().get(),
                getImage().get(),
                getDatabaseName().get(),
                reuse.getReuseBetweenBuilds().get(),
                reuse.getNewDatabaseForEachTask().get()
        );
    }

    default void reuse(Action<MongoContainerReuseRequestedByUser> action) {
        var reuse = getReuse().get();
        reuse.getAllowed().set(true);
        action.execute(reuse);
    }

    default void reuse() {
        var reuse = getReuse().get();
        reuse.getAllowed().set(true);
    }

    @Override
    default ContainerType containerType() {
        return ContainerType.MONGO;
    }
}
