package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.model.ContainersRequest;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.core.ContainersApplication;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.gradle.containers.plugin.internal.ContainersRequestV2;
import io.huskit.gradle.containers.plugin.internal.RequestedContainersFromGradleUser;
import io.huskit.log.GradleLog;
import org.gradle.api.services.BuildService;

import java.io.Serializable;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    MemoizedSupplier<ContainersApplication> containersApplication = new MemoizedSupplier<>(() -> ContainersApplication.application(
            new GradleLog(ContainersBuildService.class),
            getParameters().getTestContainersDelegate().get()
    ));

    public StartedContainers containers(ContainersRequest request) {
        return containersApplication.get().containers(request);
    }

    public StartedContainers containers(ContainersRequestV2 request) {
        return this.containers(
                new ContainersRequest(
                        request.taskLog(),
                        request.projectDescription(),
                        new RequestedContainersFromGradleUser(request.requestSpec().get())
                ));
    }

    @Override
    public void close() throws Exception {
        if (containersApplication.isInitialized()) {
            containersApplication.get().close();
            containersApplication.reset();
        }
    }
}
