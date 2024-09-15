package io.huskit.containers.internal.cli;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.api.*;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.internal.HtLazyContainer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@With
@RequiredArgsConstructor
public class HtCliRun implements HtRun {

    HtCli cli;
    HtDockerImageName imgName;
    HtRunSpecImpl runSpec;
    HtCliDckrSpec dockerSpec;

    @Override
    @SneakyThrows
    public HtContainer exec() {
        var id = cli.sendCommand(
                new CliCommand(
                        runSpec.lookFor().isPresent() ? CommandType.RUN_FOLLOW : CommandType.RUN,
                        buildCommand(),
                        line -> runSpec.lookFor().isPresent() && line.contains(runSpec.lookFor().require()),
                        Predicate.not(String::isBlank),
                        runSpec.timeout().isPresent() ? runSpec.timeout().require() : Duration.ZERO
                ),
                CommandResult::singleLine
        );
        return new HtLazyContainer(
                id,
                new MemoizedSupplier<>(() -> new HtFindCliCtrsByIds(cli, Set.of(id)).stream().findFirst().orElseThrow())
        );
    }

    private List<String> buildCommand() {
        var processCmd = new ArrayList<String>(4);
        processCmd.add("docker");
        processCmd.add("run");
        processCmd.add("-d");
        runSpec.remove().ifPresent(rm -> {
            if (rm) {
                processCmd.add("--rm");
            }
        });
        runSpec.labels().ifPresent(labelMap -> labelMap.forEach((k, v) -> {
            processCmd.add("--label");
            processCmd.add("\"" + k + "=" + v + "\"");
        }));
        runSpec.env().ifPresent(envMap -> envMap.forEach((k, v) -> {
            processCmd.add("-e");
            processCmd.add("\"" + k.toUpperCase() + "=" + v + "\"");
        }));

        processCmd.add(imgName.fullName());
        runSpec.command().ifPresent(runCmd -> {
            processCmd.add(runCmd.command());
            processCmd.addAll(runCmd.args());
        });
        return processCmd;
    }
}
