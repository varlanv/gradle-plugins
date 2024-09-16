package io.huskit.containers.internal.cli;

import io.huskit.containers.api.ShellType;

public interface Shell {

    String path();

    void write(String command);

    ShellType type();

    long pid();

    String outLine();

    void close();

    default void echo(String message) {
        write("echo " + message);
    }
}
