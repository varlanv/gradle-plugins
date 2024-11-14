package io.huskit.gradle.containers.plugin.internal;

import io.huskit.common.Log;
import lombok.RequiredArgsConstructor;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class GradleLog implements Log {

    Logger log;

    public GradleLog(Class<?> type) {
        this.log = Logging.getLogger(type);
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
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + " - " + message;
    }
}
