package io.huskit.gradle.containers.plugin.internal.request;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.model.id.ContainerId;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
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

    Property<Integer> getFixedPort();

    Property<String> getImage();

    default ProjectDescription projectDescription() {
        return new ProjectDescription.Default(
                getRootProjectName().get(),
                getProjectPath().get(),
                getProjectName().get()
        );
    }

    @Override
    default void fixedPort(Integer port) {
        getFixedPort().set(port);
    }

    @Override
    default void image(String image) {
        getImage().set(image);
    }
}
