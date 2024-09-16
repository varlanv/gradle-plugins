package io.huskit.containers.internal.cli;

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

    @SneakyThrows
    public CliShell(String shellPath) {
        dockerProcess = new ProcessBuilder(shellPath).start();

        commandWriter = new BufferedWriter(new OutputStreamWriter(dockerProcess.getOutputStream()));
        commandOutputReader = new BufferedReader(new InputStreamReader(dockerProcess.getInputStream()));
    }

    @SneakyThrows
    public String outLine() {
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
