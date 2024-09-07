package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.Constants;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.api.ShouldStartBeforeSpecView;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoContainerReuseSpec;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.mongo.MongoExposedEnvironmentSpec;
import io.huskit.gradle.containers.plugin.internal.request.AbstractShouldStartBeforeSpec;
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
        var requested = objects.newInstance(MongoContainerRequestSpec.class);
        requested.getReuse().convention(reuse);
        requested.getDatabaseName().convention(Constants.Mongo.DEFAULT_DB_NAME);
        requested.getRootProjectName().convention(getRootProjectName());
        requested.getProjectPath().convention(getProjectPath());
        requested.getProjectName().convention(getProjectName());
        requested.getImage().convention(Constants.Mongo.DEFAULT_IMAGE);
        getContainersRequestedByUser().add(requested);
        var exposedEnvironment = objects.newInstance(MongoExposedEnvironmentSpec.class);
        exposedEnvironment.getConnectionString().convention(Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV);
        exposedEnvironment.getDatabaseName().convention(Constants.Mongo.DEFAULT_DB_NAME_ENV);
        exposedEnvironment.getPort().convention(Constants.Mongo.DEFAULT_PORT_ENV);
        requested.getExposedEnvironment().convention(exposedEnvironment);
        action.execute(requested);
    }

    @Override
    public void shouldStartBefore(Action<ShouldStartBeforeSpecView> action) {
        var spec = getObjects().newInstance(AbstractShouldStartBeforeSpec.class);
        action.execute(spec);
        getShouldStartBeforeSpec().set(spec);
    }
}
