package io.huskit.gradle.containers.plugin.internal.spec.mongo;

import io.huskit.common.HtConstants;
import io.huskit.common.Log;
import io.huskit.common.Mutable;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.integration.ContainerSpec;
import io.huskit.containers.integration.DefContainerSpec;
import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.image.DefaultContainerImage;
import io.huskit.containers.model.request.DefaultMongoRequestedContainer;
import io.huskit.containers.model.request.MongoExposedEnvironment;
import io.huskit.containers.model.request.MongoRequestedContainer;
import io.huskit.containers.model.reuse.ContainerCleanupOptions;
import io.huskit.containers.model.reuse.DefaultMongoContainerReuseOptions;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerReuseSpecView;
import io.huskit.gradle.containers.plugin.api.mongo.MongoExposedEnvironmentSpecView;
import io.huskit.gradle.containers.plugin.internal.HuskitContainersExtension;
import io.huskit.gradle.containers.plugin.internal.spec.CleanupSpec;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerPortSpec;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.spec.FixedContainerPortSpec;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface MongoContainerRequestSpec extends ContainerRequestSpec, MongoContainerRequestSpecView {

    Property<String> getDatabaseName();

    Property<MongoContainerReuseSpec> getReuse();

    Property<MongoExposedEnvironmentSpec> getExposedEnvironment();

    @Override
    default MongoRequestedContainer toRequestedContainer() {
        var containerReuseSpec = getReuse().get();
        var exposedEnvironmentSpec = getExposedEnvironment().get();
        var cleanupSpec = containerReuseSpec.getCleanupSpec().get();
        var port = getPort().get();
        return new DefaultMongoRequestedContainer(
            () -> getProjectPath().get(),
            new MongoExposedEnvironment.Default(
                exposedEnvironmentSpec.getConnectionString().get(),
                exposedEnvironmentSpec.getDatabaseName().get(),
                exposedEnvironmentSpec.getPort().get()
            ),
            getDatabaseName().get(),
            new DefaultContainerImage(getImage().get()),
            port.resolve(port),
            key(),
            DefaultMongoContainerReuseOptions.builder()
                .enabled(Boolean.TRUE.equals(containerReuseSpec.getEnabled().getOrNull()))
                .reuseBetweenBuilds(Boolean.TRUE.equals(containerReuseSpec.getReuseBetweenBuilds().getOrNull()))
                .newDatabaseForEachRequest(Boolean.TRUE.equals(containerReuseSpec.getNewDatabaseForEachTask().getOrNull()))
                .cleanup(ContainerCleanupOptions.after(
                    Optional.ofNullable(cleanupSpec.getCleanupAfter().getOrNull()).orElse(Duration.ZERO)))
                .build()
        );
    }

    @Override
    default void reuse(Action<MongoContainerReuseSpecView> action) {
        var reuse = getReuse().get();
        reuse.getEnabled().set(true);
        action.execute(reuse);
    }

    @Override
    @NotNull
    default Map<String, Object> keyProps() {
        var reuse = getReuse().get();
        var exposedEnv = getExposedEnvironment().get();
        var reuseEnabled = reuse.getEnabled().get();
        return Map.of(
            "rootProjectName", getRootProjectName().get(),
            "projectName", reuseEnabled ? "" : getProjectName().get(),
            "image", getImage().get(),
            "databaseName", getDatabaseName().get(),
            "reuseBetweenBuilds", reuse.getReuseBetweenBuilds().get(),
            "newDatabaseForEachTask", reuse.getNewDatabaseForEachTask().get(),
            "reuseEnabled", reuseEnabled,
            "exposedPort", exposedEnv.getPort().get(),
            "exposedConnectionString", exposedEnv.getConnectionString().get(),
            "exposedDatabaseName", exposedEnv.getDatabaseName().get()
        );
    }

    @Override
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

    default void configure(HuskitContainersExtension extension, Action<MongoContainerRequestSpecView> action) {
        var objects = extension.getObjects();
        var cleanupSpec = objects.newInstance(CleanupSpec.class);
        cleanupSpec.getCleanupAfter().convention(HtConstants.Cleanup.DEFAULT_CLEANUP_AFTER);
        var reuse = objects.newInstance(MongoContainerReuseSpec.class);
        reuse.getEnabled().convention(false);
        reuse.getNewDatabaseForEachTask().convention(false);
        reuse.getReuseBetweenBuilds().convention(false);
        reuse.getCleanupSpec().convention(cleanupSpec);
        var exposedEnvironment = objects.newInstance(MongoExposedEnvironmentSpec.class);
        exposedEnvironment.getConnectionString().convention(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV);
        exposedEnvironment.getDatabaseName().convention(HtConstants.Mongo.DEFAULT_DB_NAME_ENV);
        exposedEnvironment.getPort().convention(HtConstants.Mongo.DEFAULT_PORT_ENV);
        var port = objects.newInstance(ContainerPortSpec.class);
        var fixedPort = objects.newInstance(FixedContainerPortSpec.class);
        var containerDefaultPort = port.getContainerDefaultPort();
        fixedPort.getContainerValue().convention(containerDefaultPort);
        port.getFixed().convention(fixedPort);
        port.getDynamic().convention(true);
        containerDefaultPort.set(HtConstants.Mongo.DEFAULT_PORT);
        getReuse().convention(reuse);
        getDatabaseName().convention(HtConstants.Mongo.DEFAULT_DB_NAME);
        getRootProjectName().convention(extension.getRootProjectName());
        getProjectPath().convention(extension.getProjectPath());
        getProjectName().convention(extension.getProjectName());
        getImage().convention(HtConstants.Mongo.DEFAULT_IMAGE);
        getExposedEnvironment().convention(exposedEnvironment);
        getPort().convention(port);
        action.execute(this);
    }

    @Override
    default ContainerSpec toContainerSpec() {
        var containerSpec = new DefContainerSpec(
            Mutable.of(Log.noop()),
            HtImgName.of(getImage().get()),
            containerType()
        );
        var reuseSpec = getReuse().get();
        var cleanupSpec = reuseSpec.getCleanupSpec().get();
        var reuseEnabled = reuseSpec.getEnabled().get();
        if (reuseEnabled) {
            containerSpec.reuse().enabledWithCleanupAfter(cleanupSpec.getCleanupAfter().get());
        } else {
            containerSpec.reuse().disabled();
        }
        var portSpec = getPort().get();
        if (portSpec.getDynamic().get()) {
            containerSpec.ports().dynamic(portSpec.getContainerDefaultPort().get());
        } else {
            var fixedPortSpec = portSpec.getFixed().get();
            Optional.ofNullable(fixedPortSpec.getHostRange().getOrNull())
                .ifPresent(
                    fixedRange ->
                        containerSpec.ports().range(
                            fixedRange.left(),
                            fixedRange.right(),
                            fixedPortSpec.getContainerValue().get()
                        )
                );
            Optional.ofNullable(fixedPortSpec.getHostValue().getOrNull())
                .ifPresent(
                    fixedValue ->
                        containerSpec.ports().fixed(
                            fixedValue,
                            fixedPortSpec.getContainerValue().get()
                        )
                );
        }
        var labelsSpec = containerSpec.labels();
        labelsSpec.pair(HtConstants.GRADLE_ROOT_PROJECT, getRootProjectName().get());
        labelsSpec.pair(HtConstants.CONTAINER_LABEL, "true");
        var exposedEnvironmentSpec = getExposedEnvironment().get();
        containerSpec.addProperty(HtConstants.Mongo.DEFAULT_CONNECTION_STRING_ENV, exposedEnvironmentSpec.getConnectionString().get());
        containerSpec.addProperty(HtConstants.Mongo.DEFAULT_DB_NAME_ENV, exposedEnvironmentSpec.getDatabaseName().get());
        containerSpec.addProperty(HtConstants.Mongo.DEFAULT_PORT_ENV, exposedEnvironmentSpec.getPort().get());
        containerSpec.addProperty(HtConstants.Mongo.NEW_DB_EACH_REQUEST, reuseSpec.getNewDatabaseForEachTask().get().toString());
        if (!reuseEnabled) {
            labelsSpec.pair(HtConstants.CONTAINER_PROJECT_KEY, getProjectName().get());
        }
        return containerSpec;
    }
}
