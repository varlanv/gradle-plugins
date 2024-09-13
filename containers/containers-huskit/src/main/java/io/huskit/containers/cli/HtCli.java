package io.huskit.containers.cli;

import io.huskit.common.Environment;
import io.huskit.common.Nothing;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.api.CliRecorder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;
import lombok.experimental.NonFinal;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class HtCli {

    @With
    CliRecorder recorder;
    MemoizedSupplier<DockerShellProcess> process = new MemoizedSupplier<>(this::createProcess);

    @SneakyThrows
    public <T> T sendCommand(HtCommand command, Function<CommandResult, T> resultConsumer) {
        var dockerShellProcess = process.get();
        return dockerShellProcess.sendCommand(command, resultConsumer);
    }

    public void sendCommand(HtCommand command, Consumer<CommandResult> resultConsumer) {
        sendCommand(command, result -> {
            resultConsumer.accept(result);
            return Nothing.instance();
        });
    }

    @SneakyThrows
    private DockerShellProcess createProcess() {
        return new DockerShellProcess(recorder);
    }

    @SneakyThrows
    public void close() {
        if (process.isInitialized()) {
            process.get().stop();
        }
    }

    static class DockerShellProcess {

        private static final String RUN_LINE_MARKER = "__HUSKIT_RUN_MARKER__";
        private static final String CLEAR_LINE_MARKER = "__HUSKIT_CLEAR_MARKER__";
        private AtomicBoolean isStopped = new AtomicBoolean();
        private Process dockerProcess;
        private BufferedWriter commandWriter;
        private BufferedReader commandOutputReader;
        private BufferedReader commandErrorReader;
        private CliRecorder recorder;
        @NonFinal
        @Nullable
        private String previousOutLine;
        @NonFinal
        @Nullable
        private String previousErrLine;

        public DockerShellProcess(CliRecorder recorder) throws IOException {
            this.recorder = recorder;
            var builder = new ProcessBuilder().redirectErrorStream(true);
            if (Environment.is(Environment.WINDOWS)) {
                builder.command("powershell");
            } else {
                builder.command("/bin/sh");
            }

            dockerProcess = builder.start();
            // Set up writers and readers for the process
            commandWriter = new BufferedWriter(new OutputStreamWriter(dockerProcess.getOutputStream()));
            commandOutputReader = new BufferedReader(new InputStreamReader(dockerProcess.getInputStream()));
            commandErrorReader = new BufferedReader(new InputStreamReader(dockerProcess.getErrorStream()));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            do {
                previousOutLine = readOutLine();
            } while (!previousOutLine.isEmpty());
        }

        public <T> T sendCommand(HtCommand command, Function<CommandResult, T> resultFunction) throws IOException {
            doSendCommand(command);
            commandWriter.write("echo " + RUN_LINE_MARKER);
            commandWriter.newLine();
            commandWriter.flush();
            return read(command, resultFunction);
        }

        @SneakyThrows
        private void doSendCommand(HtCommand command) {
            recorder.record(command);
            clearBuffer();
            commandWriter.write(String.join(" ", command.value()));
            commandWriter.newLine();
            commandWriter.flush();
        }

        @SneakyThrows
        public <T> T read(HtCommand command,
                          Function<CommandResult, T> resultConsumer) {
            var commandString = String.join(" ", command.value());
            var lines = new ArrayList<String>();
            var line = readOutLine();
            line = readOutLine();
            while (!line.endsWith(RUN_LINE_MARKER)) {
                if (!line.isEmpty()) {
                    if (command.terminatePredicate().test(line)) {
                        throw new UnsupportedOperationException();
//                        commandWriter.write("\003"); // ASCII code for "Ctrl+C"
//                        commandWriter.flush();
//                        var ctrlc = Runtime.getRuntime().exec("powershell kill " + dockerProcess.pid());
//                        ctrlc.waitFor();
//                        lines.add(line);
//                        break;
                    }
                    if (!line.endsWith(commandString) && command.linePredicate().test(line)) {
                        lines.add(line);
                    }
                }
                line = readOutLine();
            }
            previousOutLine = line;
            return resultConsumer.apply(new CommandResult(lines));
        }

        public void stop() throws IOException {
            if (isStopped.compareAndSet(false, true)) {
                dockerProcess.destroyForcibly();
                commandWriter.close();
                commandOutputReader.close();
            }
        }

        private void clearBuffer() throws IOException {
            commandWriter.write("echo " + CLEAR_LINE_MARKER);
            commandWriter.newLine();
            commandWriter.flush();
            var line = readOutLine();
            while (!Objects.equals(line, CLEAR_LINE_MARKER)) {
                line = readOutLine();
            }
            previousOutLine = line;
            while (previousErrLine != null && !previousErrLine.isEmpty()) {
                previousErrLine = commandErrorReader.readLine();
            }
        }

        private String readOutLine() throws IOException {
            return commandOutputReader.readLine();
        }
    }
}
