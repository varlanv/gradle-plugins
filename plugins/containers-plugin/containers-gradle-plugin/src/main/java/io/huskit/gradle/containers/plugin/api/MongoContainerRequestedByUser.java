package io.huskit.gradle.containers.plugin.api;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.id.DefaultContainerId;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MongoContainerRequestedByUser extends ContainerRequestedByUserForTask {

    Property<String> getDatabaseName();

    Property<MongoContainerReuseRequestedByUser> getReuse();

    default ContainerId id() {
        var reuse = getReuse().get();
        return new DefaultContainerId(
                Stream.of("image:" + getImage().get(),
                                "reuseBetweenBuilds:" + reuse.getReuseBetweenBuilds().get(),
                                "newDatabaseForEachTask:" + reuse.getNewDatabaseForEachTask().get(),
                                "databaseName:" + getDatabaseName().get()
                        )
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );
    }

    default void reuse(Action<MongoContainerReuseRequestedByUser> action) {
        MongoContainerReuseRequestedByUser reuse = getReuse().get();
        reuse.getAllowed().set(true);
        action.execute(reuse);
    }

    default void reuse() {
        MongoContainerReuseRequestedByUser reuse = getReuse().get();
        reuse.getAllowed().set(true);
    }

    @Override
    default ContainerType containerType() {
        return ContainerType.MONGO;
    }
}
