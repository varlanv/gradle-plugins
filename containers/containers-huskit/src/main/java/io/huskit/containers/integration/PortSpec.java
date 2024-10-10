package io.huskit.containers.integration;

public interface PortSpec {

    ContainerSpec dynamic(Integer containerPort);

    ContainerSpec fixed(Integer hostPort, Integer containerPort);

    ContainerSpec range(Integer hostPortFrom, Integer hostPortTo, Integer containerPort);
}
