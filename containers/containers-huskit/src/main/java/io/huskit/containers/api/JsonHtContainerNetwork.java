package io.huskit.containers.api;

import io.huskit.common.collection.HtCollections;
import io.huskit.common.port.MappedPort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JsonHtContainerNetwork implements HtContainerNetwork {

    Map<String, Object> network;

    @Override
    @SuppressWarnings("unchecked")
    public List<MappedPort> ports() {
        Map<String, Object> ports = HtCollections.getFromMap("Ports", network);
        var mappedPorts = new ArrayList<MappedPort>();
        for (var portEntry : ports.entrySet()) {
            var values = (List<Map<String, String>>) portEntry.getValue();
            var mappedPort = values.get(0);
            var hostPort = Integer.parseInt(mappedPort.get("HostPort"));
            mappedPorts.add(new MappedPort(hostPort, Integer.parseInt(portEntry.getKey().split("/")[0])));
        }
        return Collections.unmodifiableList(mappedPorts);
    }

    @Override
    public Integer firstMappedPort() {
        return ports().get(0).host();
    }
}
