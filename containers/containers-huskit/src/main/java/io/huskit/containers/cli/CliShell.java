package io.huskit.containers.cli;

import io.huskit.common.Mutable;
import io.huskit.common.Sneaky;
import io.huskit.common.io.TeeBufferedReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
class CliShell implements Shell {

    @Getter
    ShellType type;
    Process dockerProcess;
    BufferedWriter commandWriter;
    BufferedReader commandOutputReader;
    AtomicBoolean isClosed;
    Mutable<Future<?>> errReadTask = Mutable.of();

    @SneakyThrows
    public CliShell(ShellPickArg arg) {
        this.type = arg.shellType();
        var processBuilder = new ProcessBuilder(arg.shellType().pathForCurrentOs());
        this.dockerProcess = processBuilder.start();
        var cr = new BufferedReader(new InputStreamReader(dockerProcess.getInputStream()));
        if (arg.forwardStdout()) {
            this.commandOutputReader = new TeeBufferedReader(cr);
        } else {
            this.commandOutputReader = cr;
        }
        this.commandWriter = new BufferedWriter(new OutputStreamWriter(dockerProcess.getOutputStream()));
        var err = new BufferedReader(new InputStreamReader(dockerProcess.getErrorStream()));
        if (arg.forwardStderr()) {
            errReadTask.set(
                    CompletableFuture.runAsync(Sneaky.quiet(
                            () -> {
                                String line;
                                while ((line = err.readLine()) != null) {
                                    System.err.println(line);
                                }
                            }
                    ))
            );
        }
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
            errReadTask.ifPresent(f -> f.cancel(true));
            dockerProcess.destroyForcibly();
            commandWriter.close();
            commandOutputReader.close();
        }
    }
}
