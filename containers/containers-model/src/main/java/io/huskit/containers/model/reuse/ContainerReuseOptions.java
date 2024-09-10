package io.huskit.containers.model.reuse;

public interface ContainerReuseOptions {

    Boolean enabled();

    Boolean reuseBetweenBuilds();

    ContainerCleanupOptions cleanup();
}
