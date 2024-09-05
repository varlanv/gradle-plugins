package io.huskit.gradle.containers.plugin.api;

import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.model.id.ContainerId;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

public interface ContainerRequestForTaskSpec extends ContainerRequestSpec {

    ContainerId id();

    @Internal
    Property<String> getRootProjectName();

    @Internal
    Property<String> getProjectPath();

    @Internal
    Property<String> getProjectName();

    default ProjectDescription projectDescription() {
        return new ProjectDescription.Default(
                getRootProjectName().get(),
                getProjectPath().get(),
                getProjectName().get()
        );
    }
}
