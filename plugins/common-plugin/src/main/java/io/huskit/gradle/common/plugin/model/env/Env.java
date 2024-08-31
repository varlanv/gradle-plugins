package io.huskit.gradle.common.plugin.model.env;

import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;

public interface Env {

    String EXTENSION_NAME = new DefaultInternalExtensionName("env").toString();

    boolean isCi();

    boolean isLocal();

    boolean isMac();

    boolean isUnix();

    boolean isWindows();
}
