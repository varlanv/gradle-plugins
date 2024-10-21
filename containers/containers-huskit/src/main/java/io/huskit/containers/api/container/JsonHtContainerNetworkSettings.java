package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

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
}
