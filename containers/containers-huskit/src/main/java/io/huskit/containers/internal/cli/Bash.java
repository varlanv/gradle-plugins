package io.huskit.containers.internal.cli;

import io.huskit.containers.api.ShellType;

public class Bash implements Shell {

    CliShell cliShell;

    public Bash() {
        this.cliShell = new CliShell(type());
    }

    @Override
    public void write(String command) {
        cliShell.write(command);
    }

    @Override
    public ShellType type() {
        return ShellType.BASH;
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
