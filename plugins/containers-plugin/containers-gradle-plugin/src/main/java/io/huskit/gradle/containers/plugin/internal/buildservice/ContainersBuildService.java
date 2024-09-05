package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.containers.model.Containers;
import io.huskit.containers.model.request.ContainersRequest;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.gradle.common.function.MemoizedSupplier;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.core.ContainersApplication;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.log.GradleLog;
import org.gradle.api.services.BuildService;

import java.io.Serializable;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    MemoizedSupplier<ContainersApplication> containersApplication = new MemoizedSupplier<>(() -> ContainersApplication.application(
            new GradleLog(ContainersBuildService.class)
    ));

    public StartedContainers containers(ContainersRequest request) {
        return containersApplication.get().containers(request);
    }

    @Override
    public void close() throws Exception {
        if (containersApplication.isInitialized()) {
            containersApplication.get().close();
            containersApplication.reset();
        }
    }
}
