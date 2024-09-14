package io.huskit.containers.internal.cli;

import io.huskit.containers.internal.HtDefaultDockerImageName;
import io.huskit.containers.api.*;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.logs.HtCliLogs;
import io.huskit.containers.api.run.HtCliRunOptions;
import io.huskit.containers.api.run.HtRun;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtCliDckr implements HtCliDocker {

    HtCli cli;
    HtCliDockerSpec spec;

    @Override
    public HtListContainers listContainers() {
        return new HtCliListCtrs(cli, HtListContainersArgs.empty());
    }

    @Override
    public HtLogs logs(CharSequence containerId) {
        return new HtCliLogs(cli, containerId.toString());
    }

    @Override
    public HtRun run(HtDockerImageName dockerImageName) {
        return new HtCliRun(cli, dockerImageName, new HtCliRunOptions());
    }

    @Override
    public HtRun run(CharSequence dockerImageName) {
        return new HtCliRun(cli, new HtDefaultDockerImageName(dockerImageName.toString()), new HtCliRunOptions());
    }

    @Override
    public HtRm remove(CharSequence... containerIds) {
        return new HtCliRm(cli, Stream.of(containerIds).map(CharSequence::toString).collect(Collectors.toList()), false, false);
    }

    @Override
    public <T extends CharSequence> HtRm remove(List<T> containerIds) {
        return new HtCliRm(cli, containerIds.stream().map(CharSequence::toString).collect(Collectors.toList()), false, false);
    }

    @Override
    public HtCliDocker configure(Function<HtCliDockerSpec, HtCliDockerSpec> configurer) {
        var newSpec = configurer.apply(spec);
        return new HtCliDckr(
                cli.withRecorder(newSpec.recorder()),
                newSpec
        );
    }

    @Override
    public void close() {
        cli.close();
    }
}
