package io.huskit.gradle.containers.plugin.internal.spec;

import io.huskit.gradle.containers.plugin.api.ContainerReuseSpecView;
import org.gradle.api.provider.Property;

public interface ContainerReuseSpec extends ContainerReuseSpecView {

    Property<Boolean> getEnabled();

    Property<Boolean> getReuseBetweenBuilds();

    @Override
    default void enabled(boolean enabled) {
        getEnabled().set(enabled);
    }

    @Override
    default void reuseBetweenBuilds(boolean reuseBetweenBuilds) {
        getReuseBetweenBuilds().set(reuseBetweenBuilds);
    }
}
