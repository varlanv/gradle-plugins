package io.huskit.containers.cli;

import io.huskit.common.Mutable;
import io.huskit.common.Sneaky;
import io.huskit.common.Volatile;
import io.huskit.common.io.TeeBufferedReader;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@RequiredArgsConstructor
public class LogFollow {

    HtCliDckrSpec dockerSpec;
    CliRecorder recorder;
    Collection<String> containerIdsForCleanup;

    public <T> T send(HtCommand command, Function<CommandResult, T> resultFunction) {
        recorder.record(command);
        var process = Volatile.<Process>of();
        var task = listLogsTask(command, process);
        try {
            if (command.timeout().isZero()) {
                return resultFunction.apply(
                        new CommandResult(
                                task.get()
                        )
                );
            } else {
                return resultFunction.apply(
                        new CommandResult(
                                task.get(
                                        command.timeout().toMillis(),
                                        TimeUnit.MILLISECONDS
                                )
                        )
                );
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

    private CompletableFuture<List<String>> listLogsTask(HtCommand command, Volatile<Process> processHolder) {
        return CompletableFuture.supplyAsync(() -> {
            var errReadTask = Mutable.<CompletableFuture<?>>of();
            try {
                var lines = new ArrayList<String>();
                var pb = new ProcessBuilder(command.value());
                var process = pb.start();
                if (dockerSpec.forwardStderr()) {
                    errReadTask.set(
                            CompletableFuture.runAsync(
                                    Sneaky.quiet(
                                            () -> {
                                                try (var reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                                                    String line;
                                                    while ((line = reader.readLine()) != null) {
                                                        System.err.println(line);
                                                    }
                                                }
                                            }
                                    )
                            )
                    );
                }
                processHolder.set(process);
                try (var reader = getReader(process)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                        if (command.terminatePredicate().test(line)) {
                            process.destroyForcibly();
                            if (command.type() == CommandType.CONTAINERS_RUN) {
                                containerIdsForCleanup.add(lines.get(0));
                            }
                            break;
                        }
                    }
                }
                return lines;
            } catch (Exception e) {
                processHolder.ifPresent(p -> {
                    if (p.isAlive()) {
                        p.destroyForcibly();
                    }
                });
                throw Sneaky.rethrow(e);
            } finally {
                errReadTask.ifPresent(f -> f.cancel(true));
            }
        });
    }

    private BufferedReader getReader(Process process) {
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        if (dockerSpec.forwardStdout()) {
            return new TeeBufferedReader(reader);
        } else {
            return reader;
        }
    }
}
