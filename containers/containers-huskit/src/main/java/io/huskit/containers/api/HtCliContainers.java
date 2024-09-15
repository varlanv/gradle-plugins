package io.huskit.containers.api;

import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.logs.HtCliLogs;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.internal.cli.HtCli;
import io.huskit.containers.internal.cli.HtCliListCtrs;
import io.huskit.containers.internal.cli.HtCliRm;
import io.huskit.containers.internal.cli.HtCliRun;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtCliContainers implements HtContainers {

    HtCli cli;
    HtCliDckrSpec dockerSpec;

    @Override
    public HtListContainers list() {
        return new HtCliListCtrs(cli, HtListContainersArgs.empty());
    }

    @Override
    public HtListContainers list(Consumer<HtListContainersArgsSpec> args) {
        return null;
    }

    @Override
    public HtLogs logs(CharSequence containerId) {
        return new HtCliLogs(cli, containerId.toString());
    }

    @Override
    public HtRun run(CharSequence dockerImageName) {
        return new HtCliRun(
                cli,
                HtDockerImageName.of(dockerImageName.toString()),
                new HtRunSpecImpl(),
                dockerSpec
        );
    }

    @Override
    public HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec) {
        var runSpec = new HtRunSpecImpl();
        spec.accept(runSpec);
        return new HtCliRun(
                cli,
                HtDockerImageName.of(dockerImageName.toString()),
                runSpec,
                dockerSpec
        );
    }

    @Override
    public HtRm remove(CharSequence... containerIds) {
        return new HtCliRm(cli, Stream.of(containerIds).map(CharSequence::toString).collect(Collectors.toList()), false, false);
    }

    @Override
    public <T extends CharSequence> HtRm remove(List<T> containerIds) {
        return new HtCliRm(cli, containerIds.stream().map(CharSequence::toString).collect(Collectors.toList()), false, false);
    }
}
