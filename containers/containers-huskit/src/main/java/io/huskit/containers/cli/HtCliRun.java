package io.huskit.containers.cli;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtLazyContainer;
import io.huskit.containers.api.container.logs.LookFor;
import io.huskit.containers.api.container.run.HtRun;
import io.huskit.containers.model.HtConstants;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@With
@RequiredArgsConstructor
class HtCliRun implements HtRun {
    HtCliContainers parent;
    HtCli cli;
    CmdRunSpec runSpec;
    HtCliDckrSpec dockerSpec;

    @Override
    public HtContainer exec() {
        var id = cli.sendCommand(
                new CliCommand(
                        runSpec.commandType(),
                        runSpec.toCommand(),
                        HtConstants.Predicates.alwaysFalse(),
                        Predicate.not(String::isBlank),
                        runSpec.timeout()
                ),
                CommandResult::singleLine
        );
        runSpec.lookFor().ifPresent(lookFor -> parent.logs(id)
                .follow()
                .lookFor(LookFor.word(lookFor).withTimeout(runSpec.timeout()))
        );
        return new HtLazyContainer(
                id,
                MemoizedSupplier.of(() -> new HtFindCliCtrsByIds(cli, Set.of(id)).stream()
                        .findFirst()
                        .orElseThrow())
        );
    }

    @Override
    public CompletableFuture<HtContainer> execAsync() {
        return null;
    }
}
