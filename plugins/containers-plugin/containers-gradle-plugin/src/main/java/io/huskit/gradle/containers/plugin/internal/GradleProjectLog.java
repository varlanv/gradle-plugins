package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

public class GradleProjectLog implements Log {

    Logger log;
    String path;
    String name;

    public GradleProjectLog(Class<?> type, String path, String name) {
        this.log = Logging.getLogger(type);
        this.path = path;
        this.name = name;
    }

    @Override
    public void info(Supplier<String> message) {
        log.info(formatted(message.get()));
    }

    @Override
    public void debug(Supplier<String> message) {
        log.debug(formatted(message.get()));
    }

    @Override
    public void error(Supplier<String> message) {
        log.error(formatted(message.get()));
    }

    private String formatted(String message) {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + " - " + path + " - " + message;
    }
}
