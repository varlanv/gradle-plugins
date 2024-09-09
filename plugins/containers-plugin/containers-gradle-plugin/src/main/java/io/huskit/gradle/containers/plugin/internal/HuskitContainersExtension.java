package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.Constants;
import io.huskit.containers.model.exception.NonUniqueContainerException;
import io.huskit.gradle.containers.plugin.api.*;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoContainerReuseSpec;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoExposedEnvironmentSpec;
import io.huskit.gradle.containers.plugin.internal.request.AbstractShouldStartBeforeSpec;
import io.huskit.gradle.containers.plugin.internal.request.ContainerRequestSpec;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import javax.inject.Inject;

public abstract class HuskitContainersExtension implements ContainersExtension {

    public abstract ListProperty<ContainerRequestSpecView> getContainersRequestedByUser();

    public abstract Property<AbstractShouldStartBeforeSpec> getShouldStartBeforeSpec();

    @Internal
    public abstract Property<String> getRootProjectName();

    @Internal
    public abstract Property<String> getProjectName();

    @Internal
    public abstract Property<String> getProjectPath();

    @Inject
    public abstract ObjectFactory getObjects();

    @Override
    public void mongo(Action<MongoContainerRequestSpecView> action) {
        var objects = getObjects();
        var cleanupSpec = objects.newInstance(CleanupSpec.class);
        cleanupSpec.getCleanupAfter().convention(Constants.Cleanup.DEFAULT_CLEANUP_AFTER);
        var reuse = objects.newInstance(MongoContainerReuseSpec.class);
        reuse.getEnabled().convention(false);
        reuse.getNewDatabaseForEachTask().convention(false);
        reuse.getReuseBetweenBuilds().convention(false);
        reuse.getCleanupSpec().convention(cleanupSpec);
        var exposedEnvironment = objects.newInstance(MongoExposedEnvironmentSpec.class);
        exposedEnvironment.getConnectionString().convention(Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV);
        exposedEnvironment.getDatabaseName().convention(Constants.Mongo.DEFAULT_DB_NAME_ENV);
        exposedEnvironment.getPort().convention(Constants.Mongo.DEFAULT_PORT_ENV);
        var port = objects.newInstance(ContainerPortSpec.class);
        var fixedPort = objects.newInstance(FixedContainerPortSpec.class);
        port.getFixed().convention(fixedPort);
        port.getDynamic().convention(true);
        port.getContainerDefaultPort().set(Constants.Mongo.DEFAULT_PORT);
        var requested = objects.newInstance(MongoContainerRequestSpec.class);
        requested.getReuse().convention(reuse);
        requested.getDatabaseName().convention(Constants.Mongo.DEFAULT_DB_NAME);
        requested.getRootProjectName().convention(getRootProjectName());
        requested.getProjectPath().convention(getProjectPath());
        requested.getProjectName().convention(getProjectName());
        requested.getImage().convention(Constants.Mongo.DEFAULT_IMAGE);
        requested.getExposedEnvironment().convention(exposedEnvironment);
        requested.getPort().convention(port);
        action.execute(requested);
        validateAndAdd(requested);
    }

    private void validateAndAdd(ContainerRequestSpec requested) {
        var requestedId = requested.id();
        boolean hasDuplicates = getContainersRequestedByUser().get().stream()
                .map(ContainerRequestSpec.class::cast)
                .map(ContainerRequestSpec::id)
                .anyMatch(requestedId::equals);
        if (hasDuplicates) {
            // TODO show diff to user
            throw new NonUniqueContainerException(requestedId.json(), requested.containerType());
        }
        getContainersRequestedByUser().add(requested);
    }

    @Override
    public void shouldStartBefore(Action<ShouldStartBeforeSpecView> action) {
        var spec = getObjects().newInstance(AbstractShouldStartBeforeSpec.class);
        action.execute(spec);
        getShouldStartBeforeSpec().set(spec);
    }
}
