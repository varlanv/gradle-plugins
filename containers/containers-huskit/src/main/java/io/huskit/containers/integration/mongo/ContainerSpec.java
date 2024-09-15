package io.huskit.containers.integration.mongo;

public interface ContainerSpec {

    EnvSpec env();

    WaitSpec await();
}
