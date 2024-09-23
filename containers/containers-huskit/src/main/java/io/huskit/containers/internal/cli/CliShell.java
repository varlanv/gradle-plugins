package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.ShellType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
public class CliShell implements Shell {

    @Getter
    ShellType type;
    Process dockerProcess;
    BufferedWriter commandWriter;
    BufferedReader commandOutputReader;
    AtomicBoolean isClosed;

    @SneakyThrows
    public CliShell(ShellType shellType) {
        this.type = shellType;
        this.dockerProcess = new ProcessBuilder(shellType.pathForCurrentOs()).start();
        this.commandWriter = new BufferedWriter(new OutputStreamWriter(dockerProcess.getOutputStream()));
        this.commandOutputReader = new BufferedReader(new InputStreamReader(dockerProcess.getInputStream()));
        this.isClosed = new AtomicBoolean();
    }

    @Override
    @SneakyThrows
    public String outLine() {
        return commandOutputReader.readLine();
    }

    @Override
    @SneakyThrows
    public void write(String command) {
        commandWriter.write(command);
        commandWriter.newLine();
        commandWriter.flush();

    }

    @Override
    public long pid() {
        return dockerProcess.pid();
    }

    @Override
    @SneakyThrows
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            dockerProcess.destroyForcibly();
            commandWriter.close();
            commandOutputReader.close();
        }
    }
}
