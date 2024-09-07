package io.huskit.containers.model.reuse;

public interface ContainerReuseOptions {

    boolean enabled();

    boolean reuseBetweenBuilds();

    ContainerCleanupOptions cleanup();
}
