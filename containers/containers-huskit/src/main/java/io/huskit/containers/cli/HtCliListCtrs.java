package io.huskit.containers.cli;

import io.huskit.containers.IO;
import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliListCtrs implements HtListContainers {

    HtCli cli;
    HtListContainersArgs psArgs;

    @Override
    public HtListContainers withArgs(Function<HtListContainersArgsBuilder, HtListContainersArgs> args) {
        return this.withPsArgs(Objects.requireNonNull(args.apply(new HtCliListCtrsArgsBuilder())));
    }

    @Override
    @SneakyThrows
    public Stream<HtContainer> stream() {
        var requestedIds = findIds();
        if (requestedIds.isEmpty()) {
            return Stream.empty();
        } else {
            return new HtFindCliCtrsByIds(cli, requestedIds).stream();
        }
    }

    @SneakyThrows
    private Set<String> findIds() {
        var findIdsCommand = buildFindIdsCommand();
        var process = new ProcessBuilder(findIdsCommand).start();
        var ids = new LinkedHashSet<String>();
        new IO().readLines(process.getInputStream(), ids::add);
        return ids;
    }

    private List<String> buildFindIdsCommand() {
        var command = new ArrayList<String>(4 + psArgs.size());
        command.add("docker");
        command.add("ps");
        psArgs.stream().forEach(arg -> {
            command.add(arg.name());
            command.addAll(arg.values());
        });
        command.add("--format");
        command.add("{{.ID}}");
        return command;
    }
}
