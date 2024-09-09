package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.exception.NonUniqueContainerException;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.api.ShouldStartBeforeSpecView;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpecView;
import io.huskit.gradle.containers.plugin.internal.spec.AbstractShouldStartBeforeSpec;
import io.huskit.gradle.containers.plugin.internal.spec.ContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.spec.mongo.MongoContainerRequestSpec;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

public interface HuskitContainersExtension extends ContainersExtension {

    /**
     * Default extension name.
     *
     * @return extension name
     */
    static String name() {
        return "huskitContainers";
    }

    ListProperty<ContainerRequestSpec> getContainersRequestedByUser();

    Property<AbstractShouldStartBeforeSpec> getShouldStartBeforeSpec();

    Property<String> getRootProjectName();

    Property<String> getProjectName();

    Property<String> getProjectPath();

    Property<Boolean> getSynchronize();

    @Inject
    ObjectFactory getObjects();

    @Override
    default void mongo(Action<MongoContainerRequestSpecView> action) {
        var requested = getObjects().newInstance(MongoContainerRequestSpec.class);
        requested.configure(this, action);
        validateAndAdd(requested);
    }

    @Override
    default void shouldStartBefore(Action<ShouldStartBeforeSpecView> action) {
        var shouldStartBeforeSpec = getShouldStartBeforeSpec();
        if (shouldStartBeforeSpec.isPresent()) {
            throw new IllegalStateException("`shouldStartBefore` can only be set once");
        }
        var spec = getObjects().newInstance(AbstractShouldStartBeforeSpec.class);
        action.execute(spec);
        shouldStartBeforeSpec.set(spec);
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
}
