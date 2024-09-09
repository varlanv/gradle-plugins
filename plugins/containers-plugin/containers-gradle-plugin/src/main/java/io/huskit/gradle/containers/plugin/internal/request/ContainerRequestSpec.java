package io.huskit.gradle.containers.plugin.internal.request;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.gradle.containers.plugin.api.ContainerPortSpec;
import io.huskit.gradle.containers.plugin.api.ContainerPortSpecView;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

public interface ContainerRequestSpec extends ContainerRequestSpecView {

    ContainerType containerType();

    ContainerId id();

    @Internal
    Property<String> getRootProjectName();

    @Internal
    Property<String> getProjectPath();

    @Internal
    Property<String> getProjectName();

    Property<String> getImage();

    Property<ContainerPortSpec> getPort();

    default ProjectDescription projectDescription() {
        return new ProjectDescription.Default(
                getRootProjectName().get(),
                getProjectPath().get(),
                getProjectName().get()
        );
    }

    @Override
    default void port(Action<ContainerPortSpecView> portAction) {
        portAction.execute(getPort().get());
    }

    @Override
    default void image(String image) {
        getImage().set(image);
    }
}
