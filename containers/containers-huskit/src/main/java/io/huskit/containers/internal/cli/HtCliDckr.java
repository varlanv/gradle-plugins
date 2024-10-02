package io.huskit.containers.internal.cli;

import io.huskit.containers.api.HtCliVolumes;
import io.huskit.containers.api.cli.HtCliContainers;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.cli.HtCliDocker;
import io.huskit.containers.api.cli.HtCliDockerSpec;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class HtCliDckr implements HtCliDocker {

    HtCli cli;
    HtCliDckrSpec spec;

    @Override
    public HtCliDckr configure(Consumer<HtCliDockerSpec> configureAction) {
        var spec = new HtCliDckrSpec(this.spec);
        configureAction.accept(spec);
        var newSpec = new HtCliDckrSpec(spec);
        return new HtCliDckr(
                cli.withDockerSpec(newSpec),
                newSpec
        );
    }

    @Override
    public void close() {
        cli.close();
    }

    @Override
    public HtCliDckr withCleanOnClose(Boolean cleanOnClose) {
        return new HtCliDckr(
                cli,
                spec.withCleanOnClose(cleanOnClose)
        );
    }

    @Override
    public HtCliContainers containers() {
        return new HtCliContainers(cli, spec);
    }

    @Override
    public HtCliImages images() {
        return new HtCliImages(cli, spec);
    }

    @Override
    public HtCliVolumes volumes() {
        return new HtCliVolumes(cli, spec);
    }
}
