package io.huskit.containers.cli;

import io.huskit.containers.api.HtRm;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class HtCliRm implements HtRm {

    HtCli cli;
    List<String> containerIds;
    @With
    Boolean frc;
    @With
    Boolean vlm;

    @Override
    public HtRm withForce(Boolean force) {
        return withFrc(force);
    }

    @Override
    public HtRm withVolumes(Boolean volumes) {
        return withVlm(volumes);
    }

    @Override
    public void exec() {
        var command = new ArrayList<String>(4);
        command.add("docker");
        command.add("rm");
        if (frc) {
            command.add("--force");
        }
        if (vlm) {
            command.add("--volumes");
        }
        command.addAll(containerIds);
        cli.sendCommand(new CliCommand(command), Function.identity());
    }
}
