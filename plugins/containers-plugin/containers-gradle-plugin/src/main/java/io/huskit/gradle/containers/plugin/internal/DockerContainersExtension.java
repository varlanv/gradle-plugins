package io.huskit.gradle.containers.plugin.internal;

import io.huskit.gradle.containers.plugin.api.*;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import javax.inject.Inject;

public abstract class DockerContainersExtension implements ContainersExtension {

    public abstract ListProperty<ContainerRequestedByUser> getContainersRequestedByUser();

    public abstract Property<ShouldStartBeforeSpec> getShouldStartBeforeSpec();

    @Internal
    public abstract Property<String> getRootProjectName();

    @Internal
    public abstract Property<String> getProjectName();

    @Internal
    public abstract Property<String> getProjectPath();

    @Inject
    public abstract ObjectFactory getObjects();

    @Override
    public void mongo(Action<MongoContainerRequestedByUser> action) {
        var requested = getObjects().newInstance(MongoContainerRequestedByUser.class);
        var reuse = getObjects().newInstance(MongoContainerReuseRequestedByUser.class);
        reuse.getEnabled().convention(false);
        reuse.getNewDatabaseForEachTask().convention(false);
        reuse.getReuseBetweenBuilds().convention(false);
        requested.getDatabaseName().convention("gradleContainerCollection");
        requested.getReuse().convention(reuse);
        requested.getRootProjectName().convention(getRootProjectName());
        requested.getProjectPath().convention(getProjectPath());
        requested.getProjectName().convention(getProjectName());
        action.execute(requested);
        getContainersRequestedByUser().add(requested);
    }

    @Override
    public void shouldStartBefore(Action<ShouldStartBefore> action) {
        var spec = getObjects().newInstance(ShouldStartBeforeSpec.class);
        action.execute(spec);
        getShouldStartBeforeSpec().set(spec);
    }
}
