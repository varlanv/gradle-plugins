package io.huskit.containers.integration;

public interface ContainerSpec {

    EnvSpec env();

    LabelSpec labels();

    WaitSpec await();

    ReuseSpec reuse();
}
