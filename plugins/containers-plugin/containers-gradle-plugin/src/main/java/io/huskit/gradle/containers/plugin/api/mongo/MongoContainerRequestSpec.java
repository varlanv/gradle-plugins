package io.huskit.gradle.containers.plugin.api.mongo;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.containers.model.id.MongoContainerId;
import io.huskit.gradle.containers.plugin.api.ContainerRequestForTaskSpec;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

public interface MongoContainerRequestSpec extends ContainerRequestForTaskSpec {

    Property<String> getDatabaseName();

    Property<MongoContainerReuseSpec> getReuse();

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

    default void reuse(Action<MongoContainerReuseSpec> action) {
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
