package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        return Instant.parse(HtCollections.getFromMap("Created", source));
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
}
