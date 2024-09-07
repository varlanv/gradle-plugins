package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.containers.model.ContainersRequest;
import io.huskit.containers.model.started.StartedContainers;
import io.huskit.containers.testcontainers.mongo.ActualTestContainersDelegate;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.core.ContainersApplication;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.gradle.containers.plugin.internal.ContainersRequestV2;
import io.huskit.gradle.containers.plugin.internal.request.RequestedContainersFromGradleUser;
import io.huskit.log.GradleLog;
import lombok.experimental.NonFinal;
import org.gradle.api.services.BuildService;

import java.io.Serializable;
import java.util.Optional;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    private volatile @NonFinal ContainersApplication application;

    @SuppressWarnings("resource")
    public StartedContainers containers(ContainersRequestV2 request) {
        var app = application;
        if (app == null) {
            synchronized (this) {
                app = application;
                if (app == null) {
                    application = app = ContainersApplication.application(
                            new GradleLog(ContainersBuildService.class),
                            Optional.ofNullable(request.testContainersDelegate()).orElseGet(ActualTestContainersDelegate::new)
                    );
                }
            }
        }
        return app.containers(
                new ContainersRequest(
                        request.taskLog(),
                        request.projectDescription(),
                        new RequestedContainersFromGradleUser(request.requestSpec().get())
                )
        );
    }

    @Override
    public void close() throws Exception {
        var app = application;
        if (app != null) {
            app.close();
        }
    }
}
