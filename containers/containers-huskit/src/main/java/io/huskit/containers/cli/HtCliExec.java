package io.huskit.containers.cli;

import io.huskit.containers.api.container.exec.HtExec;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
class HtCliExec implements HtExec {

    HtCli cli;
    HtCliExecSpec spec;

    @Override
    public void exec() {
//        return cli.sendCommand(
//                new CliCommand(
//                        CommandType.CONTAINERS_EXEC,
//                        spec.toCommand()
//                ),
//                Function.identity()
//        );
    }

    @Override
    public CompletableFuture<Void> execAsync() {
        return null;
//        return CompletableFuture.supplyAsync(this::exec);
    }
}
