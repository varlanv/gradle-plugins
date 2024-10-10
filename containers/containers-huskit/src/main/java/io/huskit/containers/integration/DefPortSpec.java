package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import io.huskit.common.port.ContainerPort;
import io.huskit.common.port.DynamicContainerPort;
import io.huskit.common.port.FixedContainerPort;
import io.huskit.common.port.FixedRangePort;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefPortSpec implements PortSpec {

    Mutable<ContainerPort> port = Mutable.of();
    ContainerSpec parent;

    @Override
    public ContainerSpec dynamic(Integer containerPort) {
        port.set(new FixedContainerPort(new DynamicContainerPort().hostValue(), containerPort));
        return parent;
    }

    @Override
    public ContainerSpec fixed(Integer hostPort, Integer containerPort) {
        port.set(new FixedContainerPort(hostPort, containerPort));
        return parent;
    }

    @Override
    public ContainerSpec range(Integer hostPortFrom, Integer hostPortTo, Integer containerPort) {
        port.set(new FixedRangePort(hostPortFrom, hostPortTo, containerPort));
        return parent;
    }
}
