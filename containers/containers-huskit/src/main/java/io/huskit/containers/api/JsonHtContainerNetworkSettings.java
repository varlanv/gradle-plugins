package io.huskit.containers.api;

import io.huskit.common.collection.HtCollections;
import io.huskit.common.port.MappedPort;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class JsonHtContainerNetworkSettings implements HtContainerNetworkSettings {

    Map<String, Object> network;

    @Override
    public Boolean hairpinMode() {
        return HtCollections.getFromMap("HairpinMode", network);
    }

    @Override
    public String sandboxKey() {
        return HtCollections.getFromMap("SandboxKey", network);
    }

    @Override
    public String sandboxId() {
        return HtCollections.getFromMap("SandboxID", network);
    }

    @Override
    public String globalIpv6Address() {
        return HtCollections.getFromMap("GlobalIPv6Address", network);
    }

    @Override
    public Integer globalIpv6PrefixLen() {
        return HtCollections.getFromMap("GlobalIPv6PrefixLen", network);
    }

    @Override
    public Integer ipPrefixLen() {
        return HtCollections.getFromMap("IPPrefixLen", network);
    }

    @Override
    public String macAddress() {
        return HtCollections.getFromMap("MacAddress", network);
    }

    @Override
    public String linkLocalIpv6Address() {
        return HtCollections.getFromMap("LinkLocalIPv6Address", network);
    }

    @Override
    public Integer linkLocalIpv6PrefixLen() {
        return HtCollections.getFromMap("LinkLocalIPv6PrefixLen", network);
    }

    @Override
    public String gateway() {
        return HtCollections.getFromMap("Gateway", network);
    }

    @Override
    public String endpointId() {
        return HtCollections.getFromMap("EndpointID", network);
    }

    @Override
    public Optional<String> secondaryIpV6Addresses() {
        var val = network.get("SecondaryIPv6Addresses");
        if (val == null) {
            return Optional.empty();
        }
        return Optional.of((String) val);
    }

    @Override
    public String ipv6Gateway() {
        return HtCollections.getFromMap("IPv6Gateway", network);
    }

    @Override
    public Optional<String> secondaryIpAddresses() {
        var val = network.get("SecondaryIPAddresses");
        if (val == null) {
            return Optional.empty();
        }
        return Optional.of((String) val);
    }

    @Override
    public String ipAddress() {
        return HtCollections.getFromMap("IPAddress", network);
    }

    @Override
    public String bridge() {
        return HtCollections.getFromMap("Bridge", network);
    }

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
        Map<String, Object> ports = HtCollections.getFromMap("Ports", network);
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
