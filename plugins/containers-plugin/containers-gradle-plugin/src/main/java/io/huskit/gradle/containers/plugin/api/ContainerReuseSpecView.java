package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.Action;

/**
 * Configuration for reusing a container.
 */
public interface ContainerReuseSpecView {

    void enabled(boolean enabled);

    void reuseBetweenBuilds(boolean reuseBetweenBuilds);

    void cleanup(Action<CleanupSpecView> action);
}
