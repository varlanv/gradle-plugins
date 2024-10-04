package io.huskit.containers.api;

import io.huskit.common.collection.HtCollections;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class JsonHtContainerConfig implements HtContainerConfig {

    Map<String, Object> config;

    @Override
    public Optional<String> entrypoint() {
        return Optional.ofNullable((String) config.get("Entrypoint"));
    }

    @Override
    public Boolean attachStder() {
        return HtCollections.getFromMap("AttachStderr", config);
    }

    @Override
    public Boolean attachStdin() {
        return HtCollections.getFromMap("AttachStdin", config);
    }

    @Override
    public String hostname() {
        return HtCollections.getFromMap("Hostname", config);
    }

    @Override
    public Boolean openStdin() {
        return HtCollections.getFromMap("OpenStdin", config);
    }

    @Override
    public Optional<String> workingDir() {
        String workingDir = HtCollections.getFromMap("WorkingDir", config);
        if (workingDir.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(workingDir);
        }
    }

    @Override
    public Map<String, String> labels() {
        return Collections.unmodifiableMap(HtCollections.getFromMap("Labels", config));
    }

    @Override
    public Map<String, String> env() {
        List<String> envList = HtCollections.getFromMap("Env", config);
        var envMap = new HashMap<String, String>(envList.size());
        for (var env : envList) {
            var split = env.split("=");
            envMap.put(split[0], split[1]);
        }
        return Collections.unmodifiableMap(envMap);
    }

    @Override
    public List<String> cmd() {
        return Collections.unmodifiableList(HtCollections.getFromMap("Cmd", config));
    }

    @Override
    public Boolean tty() {
        return HtCollections.getFromMap("Tty", config);
    }
}
