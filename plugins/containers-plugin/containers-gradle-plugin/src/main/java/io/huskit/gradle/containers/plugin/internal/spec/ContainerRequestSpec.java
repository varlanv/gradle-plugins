package io.huskit.gradle.containers.plugin.internal.spec;

import io.huskit.containers.model.ContainerType;
import io.huskit.containers.model.id.ContainerKey;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.gradle.containers.plugin.api.ContainerPortSpecView;
import io.huskit.gradle.containers.plugin.api.ContainerRequestSpecView;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface ContainerRequestSpec extends ContainerRequestSpecView, Named {

    ContainerType containerType();

    @Internal
    Property<String> getRootProjectName();

    @Internal
    Property<String> getProjectPath();

    @Internal
    Property<String> getProjectName();

    Property<String> getImage();

    Property<ContainerPortSpec> getPort();

    @NotNull
    Map<String, Object> keyProps();

    RequestedContainer toRequestedContainer();

    @Override
    default void port(Action<ContainerPortSpecView> portAction) {
        portAction.execute(getPort().get());
    }

    @Override
    default void image(String image) {
        getImage().set(image);
    }

    @NotNull
    default ContainerKey key() {
        return ContainerKey.of(keyProps());
    }

    @NotNull
    @Override
    default String getName() {
        return key().json();
    }
}
