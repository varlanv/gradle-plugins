package io.huskit.containers.internal.cli;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.run.HtCmdRunSpecImpl;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.internal.HtLazyContainer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;

import java.util.Set;
import java.util.function.Predicate;

@With
@RequiredArgsConstructor
public class HtCliRun implements HtRun {

    HtCli cli;
    HtCmdRunSpecImpl runSpec;
    HtCliDckrSpec dockerSpec;

    @Override
    @SneakyThrows
    public HtContainer exec() {
        var id = cli.sendCommand(
                new CliCommand(
                        runSpec.commandType(),
                        runSpec.build(),
                        line -> runSpec.lookFor().check(line::contains),
                        Predicate.not(String::isBlank),
                        runSpec.timeout()
                ),
                CommandResult::singleLine
        );
        return new HtLazyContainer(
                id,
                new MemoizedSupplier<>(() -> new HtFindCliCtrsByIds(cli, Set.of(id)).stream().findFirst().orElseThrow())
        );
    }
}
