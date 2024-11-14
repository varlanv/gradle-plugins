package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import io.huskit.common.port.MappedPort;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
public final class HtJsonContainer implements HtContainer {

    Map<String, Object> source;

    @Override
    public String id() {
        return HtCollections.getFromMap("Id", source);
    }

    @Override
    public String name() {
        return HtCollections.getFromMap("Name", source);
    }

    @Override
    public JsonHtContainerConfig config() {
        return new JsonHtContainerConfig(HtCollections.getFromMap("Config", source));
    }

    @Override
    public HtContainerNetworkSettings network() {
        return new JsonHtContainerNetworkSettings(HtCollections.getFromMap("NetworkSettings", source));
    }

    @Override
    public Instant createdAt() {
        Object created = HtCollections.getFromMap("Created", source);
        if (created instanceof String) {
            return Instant.parse((String) created);
        } else if (created instanceof Number) {
            return Instant.ofEpochSecond(((Number) created).longValue());
        } else {
            throw new IllegalStateException("Failed to parse created date: " + created);
        }
    }

    @Override
    public List<String> args() {
        return HtCollections.getFromMap("Args", source);
    }

    @Override
    public String path() {
        return HtCollections.getFromMap("Path", source);
    }

    @Override
    public String processLabel() {
        return HtCollections.getFromMap("ProcessLabel", source);
    }

    @Override
    public String platform() {
        return HtCollections.getFromMap("Platform", source);
    }

    @Override
    public String driver() {
        return HtCollections.getFromMap("Driver", source);
    }

    @Override
    public HtContainerGraphDriver graphDriver() {
        return new JsonHtContainerGraphDriver(HtCollections.getFromMap("GraphDriver", source));
    }

    @Override
    public String hostsPath() {
        return HtCollections.getFromMap("HostsPath", source);
    }

    @Override
    public String hostnamePath() {
        return HtCollections.getFromMap("HostnamePath", source);
    }

    @Override
    public Integer restartCount() {
        return HtCollections.getFromMap("RestartCount", source);
    }

    @Override
    public String mountLabel() {
        return HtCollections.getFromMap("MountLabel", source);
    }

    @Override
    public HtContainerState state() {
        return new JsonHtContainerState(HtCollections.getFromMap("State", source));
    }

    @Override
    public HtContainerHostConfig hostConfig() {
        return new JsonHtContainerHostConfig(HtCollections.getFromMap("HostConfig", source));
    }

    @Override
    public String resolvConfPath() {
        return HtCollections.getFromMap("ResolvConfPath", source);
    }

    @Override
    public String logPath() {
        return HtCollections.getFromMap("LogPath", source);
    }

    @Override
    public Map<String, Object> toJsonMap() {
        return Collections.unmodifiableMap(source);
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
    public List<MappedPort> ports() {
        return portsFromHostConfig()
            .or(this::portsFromSource)
            .orElse(List.of());
    }

    @SuppressWarnings("unchecked")
    private Optional<List<MappedPort>> portsFromHostConfig() {
        var hc = source.get("HostConfig");
        if (hc == null) {
            return Optional.empty();
        }
        var hostConfig = (Map<String, Object>) hc;
        var pb = hostConfig.get("PortBindings");
        if (pb == null) {
            return Optional.empty();
        }
        var ports = (Map<String, Object>) pb;
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
        return Optional.of(Collections.unmodifiableList(mappedPorts));
    }

    @SuppressWarnings("unchecked")
    private Optional<List<MappedPort>> portsFromSource() {
        var p = source.get("Ports");
        if (p == null) {
            return Optional.empty();
        }
        var ports = (List<Map<String, Object>>) p;
        var mappedPorts = new LinkedHashSet<MappedPort>();
        for (var port : ports) {
            var privatePort = Integer.parseInt(port.get("PrivatePort").toString());
            var publicPort = port.get("PublicPort");
            if (publicPort == null) {
                continue;
            }
            mappedPorts.add(new MappedPort(Integer.parseInt(publicPort.toString()), privatePort));
        }
        return Optional.of(List.copyOf(mappedPorts));
    }
}
