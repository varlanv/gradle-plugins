package io.huskit.gradle.containers.plugin.internal;

import io.huskit.gradle.containers.plugin.api.ContainerRequestSpec;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.api.ShouldStartBeforeSpec;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpec;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerReuseSpec;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import javax.inject.Inject;

public abstract class HuskitContainersExtension implements ContainersExtension {

    public abstract ListProperty<ContainerRequestSpec> getContainersRequestedByUser();

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
    public void mongo(Action<MongoContainerRequestSpec> action) {
        var requested = getObjects().newInstance(MongoContainerRequestSpec.class);
        var reuse = getObjects().newInstance(MongoContainerReuseSpec.class);
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
    public void shouldStartBefore(Action<ShouldStartBeforeSpec> action) {
        var spec = getObjects().newInstance(AbstractShouldStartBeforeSpec.class);
        action.execute(spec);
        getShouldStartBeforeSpec().set(spec);
    }
}
