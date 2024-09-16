package io.huskit.containers.internal.cli;

import io.huskit.containers.api.ShellType;
import lombok.Getter;

public class Cmd implements Shell {

    @Getter
    String path;
    CliShell cliShell;

    public Cmd(String path) {
        this.path = path;
        this.cliShell = new CliShell(path);
    }

    @Override
    public void write(String command) {
        cliShell.write(command);
    }

    @Override
    public ShellType type() {
        return ShellType.CMD;
    }

    @Override
    public long pid() {
        return cliShell.pid();
    }

    @Override
    public String outLine() {
        return cliShell.outLine();
    }

    @Override
    public void close() {
        cliShell.close();
    }
}
