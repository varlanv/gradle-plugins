package io.huskit.containers.cli;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
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
    HtListContainersArgs cmdArgs;

    @Override
    public HtListContainers withArgs(Function<HtListContainersArgsSpec, HtListContainersArgs> args) {
        return this.withCmdArgs(Objects.requireNonNull(args.apply(new HtCliListCtrsArgsSpec())));
    }

    @Override
    @SneakyThrows
    public Stream<HtContainer> asStream() {
        var requestedIds = findIds();
        if (requestedIds.isEmpty()) {
            return Stream.empty();
        } else {
            return new HtFindCliCtrsByIds(
                    cli,
                    requestedIds
            ).stream();
        }
    }

    @SneakyThrows
    private Set<String> findIds() {
        var findIdsCommand = buildFindIdsCommand();
        return new LinkedHashSet<>(
                cli.sendCommand(
                        new CliCommand(CommandType.LIST_CONTAINERS, findIdsCommand),
                        CommandResult::lines
                )
        );
    }

    private List<String> buildFindIdsCommand() {
        var staticArgsAmount = 4;
        var command = new ArrayList<String>(staticArgsAmount + cmdArgs.size());
        command.add("docker");
        command.add("ps");
        cmdArgs.stream().forEach(arg -> {
            command.add(arg.name());
            command.addAll(arg.values());
        });
        command.add("--format");
        command.add("\"{{.ID}}\"");
        return command;
    }
}
