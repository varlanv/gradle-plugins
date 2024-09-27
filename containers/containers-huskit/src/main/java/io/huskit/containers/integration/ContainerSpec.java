package io.huskit.containers.integration;

public interface ContainerSpec {

    EnvSpec env();

    WaitSpec await();
}
