package io.huskit.containers.cli;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.containers.HtDefaultDockerImageName;
import io.huskit.containers.HtLazyContainer;
import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.run.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.With;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@With
@RequiredArgsConstructor
public class HtCliRun implements HtRun {

    HtCli cli;
    HtDockerImageName imageName;
    HtRunOptions options;

    @Override
    public HtRun withImage(String image) {
        return this.withImage(new HtDefaultDockerImageName(image));
    }

    @Override
    public HtRun withImage(HtDockerImageName dockerImageName) {
        return this.withImageName(dockerImageName);
    }

    @Override
    public HtRun withOptions(Function<HtRunOptionsBuilder, HtRunOptions> options) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public HtRun withCommand(Function<HtRunCommandBuilder, HtRunCommand> command) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    @SneakyThrows
    public HtContainer exec() {
        var id = cli.sendCommand(
                new CliCommand(buildCommand()).withLinePredicate(Predicate.not(String::isBlank)),
                CommandResult::singleLine
        );
        return new HtLazyContainer(
                id,
                new MemoizedSupplier<>(() -> new HtFindCliContainersByIds(cli, Set.of(id)).stream().findFirst().orElseThrow())
        );
    }

    private List<String> buildCommand() {
        var command = new ArrayList<String>();
        command.add("docker");
        command.add("run");
        command.add("-d");
        command.add(imageName.fullName());
        return command;
    }
}
