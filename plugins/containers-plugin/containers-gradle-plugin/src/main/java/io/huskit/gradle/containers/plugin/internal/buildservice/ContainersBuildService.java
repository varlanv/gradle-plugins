package io.huskit.gradle.containers.plugin.internal.buildservice;

import io.huskit.containers.model.Containers;
import io.huskit.gradle.common.function.MemoizedSupplier;
import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;
import io.huskit.gradle.containers.plugin.internal.ContainersApplication;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import org.gradle.api.services.BuildService;

import java.io.Serializable;
import java.util.function.Supplier;

public abstract class ContainersBuildService implements BuildService<ContainersBuildServiceParams>, AutoCloseable, Serializable {

    public static String name() {
        return new DefaultInternalExtensionName("containers_build_service").toString();
    }

    Supplier<ContainersApplication> containersApplication = new MemoizedSupplier<>(ContainersApplication::application);

    public Containers containers(ContainersRequest request) {
        return containersApplication.get().containers(request);
    }

    @Override
    public void close() throws Exception {
        containersApplication.get().onBuildEnd();
    }
}
