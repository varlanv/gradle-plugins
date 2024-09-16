package io.huskit.containers.internal.cli;

import io.huskit.common.Nothing;
import io.huskit.common.Sneaky;
import io.huskit.common.Volatile;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.api.*;
import lombok.Locked;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class HtCli {

    @With
    HtCliDckrSpec dockerSpec;
    MemoizedSupplier<DockerShellProcess> process = new MemoizedSupplier<>(this::createProcess);

    @Locked
    public <T> T sendCommand(HtCommand command, Function<CommandResult, T> resultConsumer) {
        var dockerShellProcess = process.get();
        return dockerShellProcess.sendCommand(command, resultConsumer);
    }

    @Locked
    public void sendCommand(HtCommand command, Consumer<CommandResult> resultConsumer) {
        sendCommand(command, result -> {
            resultConsumer.accept(result);
            return Nothing.instance();
        });
    }

    private DockerShellProcess createProcess() {
        return new DockerShellProcess(dockerSpec);
    }

    public void close() {
        if (process.isInitialized()) {
            process.get().stop();
        }
    }

    static class DockerShellProcess {

        private static final String RUN_LINE_MARKER = "__HUSKIT_RUN_MARKER__";
        private static final String CLEAR_LINE_MARKER = "__HUSKIT_CLEAR_MARKER__";
        AtomicBoolean isStopped = new AtomicBoolean();
        CliRecorder recorder;
        Queue<String> containerIdsForCleanup;
        Boolean cleanupOnClose;
        ShellType shell;
        CliShell cliShell;

        public DockerShellProcess(HtCliDckrSpec dockerSpec) {
            this.recorder = dockerSpec.recorder();
            this.cleanupOnClose = dockerSpec.cleanOnClose();
            this.containerIdsForCleanup = new ConcurrentLinkedQueue<>();
            this.cliShell = new CliShell(dockerSpec);
            this.shell = dockerSpec.shell();
            Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        }

        public <T> T sendCommand(HtCommand command, Function<CommandResult, T> resultFunction) {
            if (command.type() == CommandType.LOGS_FOLLOW) {
                return sendFollow(command, resultFunction);
            }
            doSendCommand(command);
            cliShell.write("echo " + RUN_LINE_MARKER);
            return read(command, resultFunction);
        }

        private <T> T sendFollow(HtCommand command, Function<CommandResult, T> resultFunction) {
            recorder.record(command);
            var process = Volatile.<Process>of();
            var task = CompletableFuture.supplyAsync(() -> {
                try {
                    var lines = new ArrayList<String>();
                    process.set(new ProcessBuilder(command.value()).start());
                    try (var reader = new BufferedReader(new InputStreamReader(process.require().getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                            if (command.terminatePredicate().test(line)) {
                                process.require().destroyForcibly();
                                if (command.type() == CommandType.RUN) {
                                    containerIdsForCleanup.add(lines.get(0));
                                }
                                break;
                            }
                        }
                    }
                    return lines;
                } catch (Exception e) {
                    process.ifPresent(p -> {
                        if (p.isAlive()) {
                            p.destroyForcibly();
                        }
                    });
                    throw Sneaky.rethrow(e);
                }
            });
            try {
                if (command.timeout().isZero()) {
                    return resultFunction.apply(new CommandResult(task.get()));
                } else {
                    return resultFunction.apply(new CommandResult(task.get(command.timeout().toMillis(), TimeUnit.MILLISECONDS)));
                }
            } catch (Exception e) {
                process.ifPresent(p -> {
                    if (p.isAlive()) {
                        p.destroyForcibly();
                    }
                });
                task.cancel(true);
                throw Sneaky.rethrow(e);
            }
        }

        private void doSendCommand(HtCommand command) {
            recorder.record(command);
            clearBuffer();
            cliShell.write(String.join(" ", command.value()));
        }

        @SneakyThrows
        private <T> T read(HtCommand command,
                           Function<CommandResult, T> resultConsumer) {
            var commandString = String.join(" ", command.value());
            var lines = new ArrayList<String>();
            var line = readOutLine();
            if (shell != ShellType.GITBASH) {
                line = readOutLine();
            }
            while (!line.endsWith(RUN_LINE_MARKER)) {
                if (!line.isEmpty()) {
                    if (command.terminatePredicate().test(line)) {
                        if (shell == ShellType.POWERSHELL) {
                            var killProcess = Runtime.getRuntime().exec("powershell kill -SIGINT " + cliShell.pid());
                            killProcess.waitFor();
                        } else if (shell == ShellType.GITBASH) {
                            var killProcess = Runtime.getRuntime().exec("C:\\Program Files\\Git\\bin\\bash.exe kill " + cliShell.pid());
                            killProcess.waitFor();
                        }
                        lines.add(line);
                        break;
                    }
                    if (!line.endsWith(commandString) && command.linePredicate().test(line)) {
                        lines.add(line);
                    }
                }
                line = readOutLine();
            }
            if (command.type() == CommandType.RUN_FOLLOW) {
                var containerId = lines.get(0);
                containerIdsForCleanup.add(containerId);
                sendCommand(
                        new CliCommand(
                                CommandType.LOGS_FOLLOW,
                                List.of("docker", "logs", "-f", containerId),
                                l -> command.terminatePredicate().test(l),
                                l -> true,
                                Duration.ZERO
                        ),
                        Function.identity());
                return resultConsumer.apply(new CommandResult(List.of(containerId)));
            } else {
                if (command.type() == CommandType.RUN) {
                    containerIdsForCleanup.add(lines.get(0));
                }
                return resultConsumer.apply(new CommandResult(lines));
            }
        }

        private void stop() {
            if (isStopped.compareAndSet(false, true)) {
                if (cleanupOnClose) {
                    if (!containerIdsForCleanup.isEmpty()) {
                        var cmd = new ArrayList<String>(5);
                        cmd.add("docker");
                        cmd.add("rm");
                        cmd.add("-f");
                        cmd.add("-v");
                        cmd.addAll(containerIdsForCleanup);
                        sendCommand(
                                new CliCommand(
                                        CommandType.REMOVE_CONTAINERS,
                                        cmd,
                                        l -> false,
                                        l -> true,
                                        Duration.ofSeconds(20)
                                ),
                                Function.identity()
                        );
                    }
                }
                cliShell.close();
            }
        }

        private void clearBuffer() {
            cliShell.write("echo " + CLEAR_LINE_MARKER);
            var line = readOutLine();
            while (!Objects.equals(line, CLEAR_LINE_MARKER)) {
                line = readOutLine();
            }
        }

        private String readOutLine() {
            return cliShell.outLine();
        }
    }
}
