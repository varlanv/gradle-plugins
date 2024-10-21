package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import io.huskit.common.port.MappedPort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JsonHtContainerHostConfig implements HtContainerHostConfig {

    Map<String, Object> source;

    @Override
    public Integer firstMappedPort() {
        var ports = ports();
        var size = ports.size();
        if (size == 0) {
            throw new IllegalStateException("No mapped ports present");
        } else if (size > 1) {
            throw new IllegalStateException("Cannot pick first mapped port when multiple are present - " + ports);
        }
        return ports.get(0).host();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MappedPort> ports() {
        Map<String, Object> ports = HtCollections.getFromMap("PortBindings", source);
        var mappedPorts = new ArrayList<MappedPort>();
        for (var portEntry : ports.entrySet()) {
            var values = (List<Map<String, String>>) portEntry.getValue();
            if (values.isEmpty()) {
                throw new IllegalStateException(String.format("Port '%s' is not mapped to any host port", portEntry.getKey()));
            }
            var mappedPort = values.get(0);
            var hostPort = Integer.parseInt(mappedPort.get("HostPort"));
            mappedPorts.add(new MappedPort(hostPort, Integer.parseInt(portEntry.getKey().split("/")[0])));
        }
        return Collections.unmodifiableList(mappedPorts);
    }
}
