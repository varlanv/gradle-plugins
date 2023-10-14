package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.Log;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GradleProjectLog implements Log {

    private final Logger log;
    private final ProjectDescription projectDescription;

    public GradleProjectLog(Class<?> type, ProjectDescription projectDescription) {
        this.log = Logging.getLogger(type);
        this.projectDescription = projectDescription;
    }

    @Override
    public void info(String message) {
        log.lifecycle(formatted(message));
    }

    @Override
    public void info(String message, Object... args) {
        log.lifecycle(formatted(message), args);
    }

    @Override
    public void lifecycle(String message) {
        log.lifecycle(formatted(message));
    }

    @Override
    public void lifecycle(String message, Object... args) {
        log.lifecycle(formatted(message), args);
    }

    @Override
    public void error(String var1) {
        log.error(formatted(var1));
    }

    @Override
    public void error(String var1, Object var2) {
        log.error(formatted(var1), var2);
    }

    @Override
    public void error(String var1, Object var2, Object var3) {
        log.error(formatted(var1), var2, var3);
    }

    private String formatted(String message) {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date()) + " - " + projectDescription.path() + " - " + message;
    }
}
