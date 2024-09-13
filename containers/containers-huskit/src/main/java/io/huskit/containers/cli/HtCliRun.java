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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

@With
@RequiredArgsConstructor
public class HtCliRun implements HtRun {

    HtCli cli;
    HtDockerImageName imgName;
    HtRunOptions opts;

    @Override
    public HtRun withImage(String image) {
        return this.withImage(new HtDefaultDockerImageName(image));
    }

    @Override
    public HtRun withImage(HtDockerImageName dockerImageName) {
        return this.withImgName(dockerImageName);
    }

    @Override
    public HtRun withOptions(Function<HtRunOptions, HtRunOptions> options) {
        return this.withOpts(options.apply(this.opts));
    }

    @Override
    public HtRun withCommand(Function<HtRunCommandSpec, HtRunCommand> command) {
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
                new MemoizedSupplier<>(() -> new HtFindCliCtrsByIds(cli, Set.of(id)).stream().findFirst().orElseThrow())
        );
    }

    private List<String> buildCommand() {
        var command = new ArrayList<String>(4 + opts.size());
        command.add("docker");
        command.add("run");
        command.add("-d");
        var optionMap = opts.asMap();
        Optional.ofNullable(optionMap.get(HtOptionType.LABELS))
                .ifPresent(labelOpt -> labelOpt.map().forEach((k, v) -> {
                    command.add("--label");
                    command.add(k + "=" + v);
                }));
        command.add(imgName.fullName());
        return command;
    }
}
