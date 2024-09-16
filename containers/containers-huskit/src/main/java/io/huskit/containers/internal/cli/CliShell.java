package io.huskit.containers.internal.cli;

import io.huskit.common.Environment;
import io.huskit.containers.api.HtCliDckrSpec;
import io.huskit.containers.api.ShellType;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@RequiredArgsConstructor
public class CliShell {

    Process dockerProcess;
    BufferedWriter commandWriter;
    BufferedReader commandOutputReader;
    BufferedReader commandErrorReader;

    @SneakyThrows
    public CliShell(HtCliDckrSpec dockerSpec) {
        var builder = new ProcessBuilder();
        var shell = dockerSpec.shell();
        if (shell == ShellType.DEFAULT) {
            if (Environment.is(Environment.WINDOWS)) {
                builder.command("powershell");
            } else {
                builder.command("/bin/sh");
            }
        } else if (shell == ShellType.GITBASH) {
            builder.command("C:\\Program Files\\Git\\bin\\bash.exe");
        } else if (shell == ShellType.POWERSHELL) {
            builder.command("powershell");
        } else {
            throw new IllegalArgumentException("Unsupported shell: " + shell);
        }
        dockerProcess = builder.start();
        commandWriter = new BufferedWriter(new OutputStreamWriter(dockerProcess.getOutputStream()));
        commandOutputReader = new BufferedReader(new InputStreamReader(dockerProcess.getInputStream()));
        commandErrorReader = new BufferedReader(new InputStreamReader(dockerProcess.getErrorStream()));
    }

    @SneakyThrows
    public String nextLine() {
        return commandOutputReader.readLine();
    }

    @SneakyThrows
    public void write(String command) {
        commandWriter.write(command);
        commandWriter.newLine();
        commandWriter.flush();

    }

    public long pid() {
        return dockerProcess.pid();
    }

    @SneakyThrows
    public void close() {
        dockerProcess.destroyForcibly();
        commandWriter.close();
        commandOutputReader.close();
    }
}
