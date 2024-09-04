package io.huskit.containers.model.started;

public interface StartedContainerRegistry {

    <T extends StartedContainerInternal> T startAndRegister(String id, T startedContainerInternal, Runnable start);
}
