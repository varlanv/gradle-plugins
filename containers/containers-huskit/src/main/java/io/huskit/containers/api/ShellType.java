package io.huskit.containers.api;

import io.huskit.common.Os;
import lombok.RequiredArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public enum ShellType {

    SH(new EnumMap<>(Map.of(Os.LINUX, "sh"))),
    POWERSHELL(new EnumMap<>(Map.of(Os.WINDOWS, "powershell"))),
    CMD(new EnumMap<>(Map.of(Os.WINDOWS, "cmd"))),
    BASH(new EnumMap<>(Map.of(Os.WINDOWS, "C:\\Program Files\\Git\\bin\\bash.exe", Os.LINUX, "bash"))),
    DEFAULT(Map.of());

    private Map<Os, String> pathForOsMap;

    public String pathForCurrentOs() {
        return pathForOsMap.get(Os.current());
    }
}
