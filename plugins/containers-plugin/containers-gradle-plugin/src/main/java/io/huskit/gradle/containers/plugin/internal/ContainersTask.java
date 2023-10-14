package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.Log;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.GradleProjectLog;
import io.huskit.gradle.containers.plugin.ProjectDescription;
import io.huskit.gradle.containers.plugin.api.ContainerRequestedByUser;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.util.List;
import java.util.stream.Collectors;

@DisableCachingByDefault(because = "Caching of containers is not supported")
public abstract class ContainersTask extends DefaultTask {

    public static String NAME = "startContainers";

    @Internal
    public abstract Property<ContainersBuildService> getContainers();

    @Internal
    public abstract Property<ProjectDescription> getProjectDescription();

    @Input
    public abstract ListProperty<ContainerRequestedByUser> getRequestedContainers();


    @TaskAction
    public void startContainers() {
        ProjectDescription projectDescription = getProjectDescription().get();
        RequestedContainers requestedContainers = new RequestedContainersFromGradleUser(
                new GradleProjectLog(RequestedContainersFromGradleUser.class, projectDescription),
                getRequestedContainers().get()
        );
        List<StartedContainer> list = getContainers().get().containers(
                projectDescription,
                requestedContainers
        ).start().list();
        Log log = new GradleProjectLog(ContainersTask.class, projectDescription);
        if (list.isEmpty()) {
            log.info("No containers were started");
        } else {
            log.info("Started [{}] containers: [{}]", list.size(), list.stream()
                    .map(StartedContainer::id)
                    .collect(Collectors.toList())
            );
        }
    }
}
