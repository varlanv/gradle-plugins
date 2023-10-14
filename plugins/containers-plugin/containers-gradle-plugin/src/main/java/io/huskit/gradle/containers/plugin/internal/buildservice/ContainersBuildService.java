package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.containers.model.Containers;
import io.huskit.containers.model.request.RequestedContainers;
import io.huskit.containers.model.Log;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.plugin.GradleLog;
import io.huskit.gradle.containers.plugin.GradleProjectLog;
import io.huskit.gradle.containers.plugin.ProjectDescription;
import io.huskit.gradle.containers.plugin.internal.ContainersApplication;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import org.gradle.api.services.BuildService;

import java.io.Serializable;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    public static String name() {
        return DefaultInternalExtensionName.value("containers_build_service");
    }

    private transient volatile ContainersApplication containersApplication;

    public Containers containers(ProjectDescription projectDescription,
                                 RequestedContainers requestedContainers) {
        Log log = new GradleProjectLog(ContainersBuildService.class, projectDescription);
        return getContainersApplication(log).containers(projectDescription, requestedContainers);
    }

    @Override
    public void close() throws Exception {
        containersApplication.stop();
    }

    private ContainersApplication getContainersApplication(Log log) {
        if (containersApplication == null) {
            synchronized (this) {
                if (containersApplication == null) {
                    log.info("containersApplication is not created, entering synchronized block to create instance");
                    containersApplication = new ContainersApplication(new GradleLog(ContainersApplication.class));
                }
            }
        }
        return containersApplication;
    }
}

//public interface DockerContainerProperties {
//
//    Property<String> getImage();
//
//    Property<Boolean> getAllowDuplicates(); //todo implement it?
//
//    String key();
//
//
//    default String getKey(Stream<Provider<?>> providerStream) {
//        return providerStream.map(Provider::get)
//                .map(String::valueOf)
//                .collect(Collectors.joining());
//    }
//}


//public interface MongoDockerContainerProperties extends DockerContainerProperties {
//
//    String DEFAULT_IMAGE = "mongo:3.6.23";
//    String DEFAULT_URL_ENVIRONMENT = "SPRING_DATA_MONGODB_URI";
//
//    Property<String> getDatabaseName();
//
//    Property<String> getUrlEnvironment();
//
//    @Override
//    default String key() {
//        return getKey(Stream.of(
//                this.getImage(),
//                this.getDatabaseName(),
//                this.getUrlEnvironment())
//        );
//    }
//}