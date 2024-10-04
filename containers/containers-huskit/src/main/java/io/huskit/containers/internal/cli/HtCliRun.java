package io.huskit.containers.internal.cli;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.cli.HtCliContainers;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.logs.LookFor;
import io.huskit.containers.api.run.HtCmdRunSpecImpl;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.internal.HtLazyContainer;
import io.huskit.containers.model.HtConstants;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.Set;
import java.util.function.Predicate;

@With
@RequiredArgsConstructor
public class HtCliRun implements HtRun {
    HtCliContainers parent;
    HtCli cli;
    HtCmdRunSpecImpl runSpec;
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
                .await());
        return new HtLazyContainer(
                id,
                new MemoizedSupplier<>(() -> new HtFindCliCtrsByIds(cli, Set.of(id)).stream()
                        .findFirst()
                        .orElseThrow())
        );
    }
}
