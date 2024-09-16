package io.huskit.containers.internal.cli;

import io.huskit.common.Sneaky;
import io.huskit.common.Volatile;
import io.huskit.containers.api.CliRecorder;
import io.huskit.containers.api.CommandType;
import io.huskit.containers.api.HtCommand;
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

    private CompletableFuture<List<String>> listLogsTask(HtCommand command, Volatile<Process> process) {
        return CompletableFuture.supplyAsync(() -> {
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
    }
}
