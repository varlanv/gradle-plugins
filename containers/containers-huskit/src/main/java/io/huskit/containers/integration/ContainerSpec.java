package io.huskit.containers.integration;

import io.huskit.containers.model.ContainerType;

public interface ContainerSpec {

    EnvSpec env();

    LabelSpec labels();

    WaitSpec await();

    ReuseSpec reuse();

    PortSpec ports();

    ContainerType containerType();

    String hash();
}
