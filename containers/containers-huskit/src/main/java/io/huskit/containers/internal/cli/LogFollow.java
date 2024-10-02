package io.huskit.containers.internal.cli;

import io.huskit.common.Sneaky;
import io.huskit.common.Volatile;
import io.huskit.containers.api.cli.CliRecorder;
import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.cli.HtCommand;
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

    private CompletableFuture<List<String>> listLogsTask(HtCommand command, Volatile<Process> process) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var lines = new ArrayList<String>();
                var p = new ProcessBuilder(command.value()).start();
                process.set(p);
                try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                        if (command.terminatePredicate().test(line)) {
                            p.destroyForcibly();
                            if (command.type() == CommandType.CONTAINERS_RUN) {
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
    }
}
