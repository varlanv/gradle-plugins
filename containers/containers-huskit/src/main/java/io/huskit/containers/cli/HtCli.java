package io.huskit.containers.cli;

import io.huskit.common.Environment;
import io.huskit.common.Nothing;
import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class HtCli {

    Supplier<DockerShellProcess> process = new MemoizedSupplier<>(this::createProcess);

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
        return new DockerShellProcess();
    }

    static class DockerShellProcess {

        public static void main(String[] args) throws Exception {
            Process process = null;
            try {
                process = new ProcessBuilder("cmd").start();
                BufferedWriter commandWriter;
                BufferedReader commandOutputReader;
                commandWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                commandOutputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                commandWriter.write("docker logs -f 6bf7e253c7d7");
                commandWriter.newLine();
                commandWriter.flush();

                String line;
                while ((line = commandOutputReader.readLine()) != null) {
                    System.out.println(line);
                }

            } finally {
                if (process != null) {
                    process.destroyForcibly();
                }
            }
        }

        private static final String RUN_LINE_MARKER = "__HUSKIT_RUN_MARKER__";
        private static final String CLEAR_LINE_MARKER = "__HUSKIT_CLEAR_MARKER__";
        private Process dockerProcess;
        private BufferedWriter commandWriter;
        private BufferedReader commandOutputReader;
        private BufferedReader commandErrorReader;
        @NonFinal
        @Nullable
        private String previousOutLine;
        @NonFinal
        @Nullable
        private String previousErrLine;

        public DockerShellProcess() throws IOException {
            ProcessBuilder builder = new ProcessBuilder().redirectErrorStream(true);
            if (Environment.is(Environment.WINDOWS)) {
                builder.command("cmd");
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

        public <T> T sendCommand(HtCommand command, Function<CommandResult, T> resultTFunction) throws IOException {
            doSendCommand(command);
            commandWriter.write("echo " + RUN_LINE_MARKER);
            commandWriter.newLine();
            commandWriter.flush();
            return read(command, resultTFunction);
        }

        @SneakyThrows
        private void doSendCommand(HtCommand command) {
            clearBuffer();
            commandWriter.write(String.join(" ", command.value()));
            commandWriter.newLine();
            commandWriter.flush();
        }

        public <T> T read(HtCommand command,
                          Function<CommandResult, T> resultConsumer) throws IOException {
            var lines = new ArrayList<String>();
            var line = readOutLine();
            line = readOutLine();
            while (!line.endsWith(RUN_LINE_MARKER)) {
                if (command.terminatePredicate().test(line)) {
                    commandWriter.write("\003"); // ASCII code for "Ctrl+C"
                    commandWriter.flush();
                    clearBuffer();
                    lines.add(line);
                    break;
                }
                if (!line.endsWith(String.join(" ", command.value())) && command.linePredicate().test(line)) {
                    lines.add(line);
                }
                line = readOutLine();
            }
            previousOutLine = line;
            return resultConsumer.apply(new CommandResult(lines));
        }

        public void stop() throws IOException {
            dockerProcess.destroyForcibly();
            commandWriter.close();
            commandOutputReader.close();
        }

        private void clearBuffer() throws IOException {
            commandWriter.write("echo " + CLEAR_LINE_MARKER);
            commandWriter.newLine();
            commandWriter.flush();
            var line = readOutLine();
            while (!line.equals(CLEAR_LINE_MARKER)) {
                line = readOutLine();
            }
            previousOutLine = line;
            while (previousErrLine != null && !previousErrLine.isEmpty()) {
                previousErrLine = commandErrorReader.readLine();
            }
        }

        private String readOutLine() throws IOException {
            String line = commandOutputReader.readLine();
            System.out.println(line);
            return line;
        }
    }
}
