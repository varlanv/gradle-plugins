package io.huskit.containers.internal.cli;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.cli.HtArg;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCliListCtrs implements HtListContainers {

    HtCli cli;
    List<HtArg> cmdArgs;

    @Override
    public HtListContainers withArgs(Consumer<HtListContainersArgsSpec> args) {
        var spec = new HtCliListCtrsArgsSpec();
        args.accept(spec);
        return this.withCmdArgs(Objects.requireNonNull(spec.build()));
    }

    @Override
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

    private Set<String> findIds() {
        return new LinkedHashSet<>(
                cli.sendCommand(
                        new CliCommand(
                                CommandType.CONTAINERS_LIST,
                                new FindIdsCommand(
                                        cmdArgs
                                ).list()
                        ),
                        CommandResult::lines
                )
        );
    }
}
