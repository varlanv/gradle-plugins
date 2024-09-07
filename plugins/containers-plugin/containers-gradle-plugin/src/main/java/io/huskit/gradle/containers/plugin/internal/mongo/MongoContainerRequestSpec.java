package io.huskit.gradle.containers.plugin.internal.mongo;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerReuseSpecView;
import io.huskit.gradle.containers.plugin.api.mongo.MongoExposedEnvironmentSpecView;
import io.huskit.gradle.containers.plugin.internal.DefaultContainerId;
import io.huskit.gradle.containers.plugin.internal.request.ContainerRequestSpec;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface MongoContainerRequestSpec extends ContainerRequestSpec, MongoContainerRequestSpecView {

    @NotNull
    default ContainerId id() {
        var reuse = getReuse().get();
        var exposedEnv = getExposedEnvironment().get();
        var reuseEnabled = reuse.getEnabled().get();
        return new DefaultContainerId(this).with(Map.of(
                "projectName", reuseEnabled ? "" : getProjectName().get(),
                "databaseName", getDatabaseName().get(),
                "reuseBetweenBuilds", reuse.getReuseBetweenBuilds().get(),
                "newDatabaseForEachTask", reuse.getNewDatabaseForEachTask().get(),
                "reuseEnabled", reuseEnabled,
                "exposedPort", exposedEnv.getPort().get(),
                "exposedConnectionString", exposedEnv.getConnectionString().get(),
                "exposedDatabaseName", exposedEnv.getDatabaseName().get()
        ));
    }

    Property<String> getDatabaseName();

    Property<MongoContainerReuseSpec> getReuse();

    Property<MongoExposedEnvironmentSpec> getExposedEnvironment();


    default void reuse(Action<MongoContainerReuseSpecView> action) {
        var reuse = getReuse().get();
        reuse.getEnabled().set(true);
        action.execute(reuse);
    }

    default void exposedEnvironment(Action<MongoExposedEnvironmentSpecView> action) {
        var exposedEnvironment = getExposedEnvironment().get();
        action.execute(exposedEnvironment);
    }

    @Override
    default ContainerType containerType() {
        return ContainerType.MONGO;
    }

    @Override
    default void databaseName(String databaseName) {
        getDatabaseName().set(databaseName);
    }
}
