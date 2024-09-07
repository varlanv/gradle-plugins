package io.huskit.containers.model.started;

public interface ContainerStarter {

    <T extends NonStartedContainer, R extends StartedContainer> R start(T container);
}
