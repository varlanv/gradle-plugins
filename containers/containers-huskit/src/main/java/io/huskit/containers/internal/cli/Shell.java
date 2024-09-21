package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.ShellType;

import java.util.Objects;

public interface Shell {

    void write(String command);

    ShellType type();

    long pid();

    String outLine();

    void close();

    default void echo(String message) {
        write("echo " + message);
    }

    default void clearBuffer(String clearMarker) {
        echo(clearMarker);
        var line = outLine();
        while (!Objects.equals(line, clearMarker)) {
            line = outLine();
        }
    }
}
