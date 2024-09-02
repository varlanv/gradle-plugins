package io.huskit.gradle.common.plugin.model.env;

import io.huskit.gradle.common.plugin.model.props.Props;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultEnv implements Env {

    Props props;

    @Override
    public boolean isCi() {
        return props.env("CI").value() != null;
    }

    @Override
    public boolean isLocal() {
        return !isCi();
    }

    @Override
    public boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("mac");
    }

    @Override
    public boolean isUnix() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("nix") || osName.contains("nux");
    }

    @Override
    public boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("windows");
    }
}
