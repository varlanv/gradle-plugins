package io.huskit.containers.api.cli;

import io.huskit.containers.api.HtContainers;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.logs.HtCliLogs;
import io.huskit.containers.api.logs.HtLogs;
import io.huskit.containers.api.rm.HtRm;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.api.run.HtRunSpec;
import io.huskit.containers.api.run.HtCmdRunSpecImpl;
import io.huskit.containers.internal.cli.*;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class HtCliContainers implements HtContainers {

    HtCli cli;
    HtCliDckrSpec dockerSpec;

    @Override
    public HtListContainers list() {
        return new HtCliListCtrs(cli, List.of());
    }

    @Override
    public HtListContainers list(Consumer<HtListContainersArgsSpec> args) {
        return new HtCliListCtrs(cli, List.of()).withArgs(args);
    }

    @Override
    public HtLogs logs(CharSequence containerId) {
        return new HtCliLogs(cli, containerId.toString());
    }

    @Override
    public HtRun run(CharSequence dockerImageName) {
        return new HtCliRun(
                cli,
                new HtCmdRunSpecImpl(HtDockerImageName.of(dockerImageName.toString())),
                dockerSpec
        );
    }

    @Override
    public HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec) {
        var runSpec = new HtCmdRunSpecImpl(HtDockerImageName.of(dockerImageName.toString()));
        spec.accept(runSpec);
        return new HtCliRun(
                cli,
                runSpec,
                dockerSpec
        );
    }

    @Override
    public HtRm remove(CharSequence... containerIds) {
        return new HtCliRm(cli, new HtCliRmSpec(containerIds));
    }

    @Override
    public <T extends CharSequence> HtRm remove(List<T> containerIds) {
        return new HtCliRm(cli, new HtCliRmSpec(containerIds));
    }
}
