package io.huskit.containers.api.cli;

import io.huskit.containers.api.*;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.logs.HtCliLogs;
import io.huskit.containers.api.logs.HtLogs;
import io.huskit.containers.api.rm.HtRm;
import io.huskit.containers.api.run.HtCmdRunSpecImpl;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.api.run.HtRunSpec;
import io.huskit.containers.internal.cli.*;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
    public Stream<HtContainer> inspect(Iterable<? extends CharSequence> containerIds) {
        var ids = new LinkedHashSet<String>();
        for (var containerId : containerIds) {
            ids.add(containerId.toString());
        }
        if (ids.isEmpty()) {
            return Stream.empty();
        }
        return list().asStream()
                .filter(cnt -> ids.contains(cnt.id()));
    }

    @Override
    public HtContainer inspect(CharSequence containerId) {
        return this.inspect(Collections.singletonList(containerId))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No container found with id: " + containerId));
    }

    @Override
    public HtLogs logs(CharSequence containerId) {
        return new HtCliLogs(cli, containerId.toString());
    }

    @Override
    public HtRun run(CharSequence dockerImageName) {
        return new HtCliRun(
                this,
                cli,
                new HtCmdRunSpecImpl(HtImgName.ofPrefix(dockerSpec.imagePrefix(), dockerImageName)),
                dockerSpec
        );
    }

    @Override
    public HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec) {
        var runSpec = new HtCmdRunSpecImpl(HtImgName.ofPrefix(dockerSpec.imagePrefix(), dockerImageName));
        spec.accept(runSpec);
        return new HtCliRun(
                this,
                cli,
                runSpec,
                dockerSpec
        );
    }

    @Override
    public HtExec execInContainer(CharSequence containerId, CharSequence command, Iterable<? extends CharSequence> args) {
        var argsList = new ArrayList<String>();
        for (var arg : args) {
            argsList.add(arg.toString());
        }
        return new HtCliExec(
                cli,
                new HtCliExecSpec(
                        containerId.toString(),
                        command.toString(),
                        argsList
                )
        );
    }

    @Override
    public HtExec execInContainer(CharSequence containerId, CharSequence command) {
        return new HtCliExec(
                cli,
                new HtCliExecSpec(
                        containerId.toString(),
                        command.toString(),
                        List.of()
                )
        );
    }

    @Override
    public HtCliRm remove(CharSequence... containerIds) {
        return new HtCliRm(cli, new HtCliRmSpec(containerIds));
    }

    @Override
    public HtRm remove(CharSequence containerId, Consumer<HtCliRmSpec> specAction) {
        var rmSpec = new HtCliRmSpec(containerId);
        specAction.accept(rmSpec);
        return new HtCliRm(cli, rmSpec);
    }

    @Override
    public <T extends CharSequence> HtCliRm remove(Collection<T> containerIds, Consumer<HtCliRmSpec> specAction) {
        var rmSpec = new HtCliRmSpec(containerIds);
        specAction.accept(rmSpec);
        return new HtCliRm(cli, new HtCliRmSpec(containerIds));
    }
}
