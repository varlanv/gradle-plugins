package io.huskit.containers.cli;

import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtContainers;
import io.huskit.containers.api.container.HtCreate;
import io.huskit.containers.api.container.HtStart;
import io.huskit.containers.api.container.exec.HtExec;
import io.huskit.containers.api.container.list.HtListContainers;
import io.huskit.containers.api.container.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.container.logs.HtLogs;
import io.huskit.containers.api.container.rm.HtRm;
import io.huskit.containers.api.container.run.*;
import io.huskit.containers.api.image.HtImgName;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RequiredArgsConstructor
class HtCliContainers implements HtContainers {

    HtCli cli;
    HtCliDckrSpec dockerSpec;

    @Override
    public HtListContainers list() {
        return new HtCliListCtrs(cli, new HtCliListCtrsArgsSpec());
    }

    @Override
    public HtListContainers list(Consumer<HtListContainersArgsSpec> argsAction) {
        var args = new HtCliListCtrsArgsSpec();
        argsAction.accept(args);
        return new HtCliListCtrs(cli, args);
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
                new CmdRunSpec(HtImgName.ofPrefix(dockerSpec.imagePrefix(), dockerImageName)),
                dockerSpec
        );
    }

    @Override
    public HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec) {
        var runSpec = new CmdRunSpec(HtImgName.ofPrefix(dockerSpec.imagePrefix(), dockerImageName));
        spec.accept(runSpec);
        return new HtCliRun(
                this,
                cli,
                runSpec,
                dockerSpec
        );
    }

    @Override
    public HtCreate create(CharSequence dockerImageName) {
        return null;
    }

    @Override
    public HtCreate create(CharSequence dockerImageName, Consumer<HtCreateSpec> specAction) {
        return null;
    }

    @Override
    public HtStart start(CharSequence containerId) {
        return null;
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
    public HtRm remove(CharSequence containerId, Consumer<HtRmSpec> specAction) {
        var rmSpec = new HtCliRmSpec(containerId);
        specAction.accept(rmSpec);
        return new HtCliRm(cli, rmSpec);
    }

    @Override
    public HtCliRm remove(Iterable<? extends CharSequence> containerIds, Consumer<HtRmSpec> specAction) {
        var rmSpec = new HtCliRmSpec(containerIds);
        specAction.accept(rmSpec);
        return new HtCliRm(cli, new HtCliRmSpec(containerIds));
    }
}
