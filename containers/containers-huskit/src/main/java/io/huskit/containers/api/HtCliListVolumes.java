package io.huskit.containers.api;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.CommandResult;
import io.huskit.containers.internal.cli.HtCli;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtCliListVolumes implements HtListVolumes {

    HtCli cli;
    HtCliListVolumesSpec listVolumesSpec;
    HtCliVolumes parent;

    @Override
    public Stream<HtVolumeView> stream() {
        var ids = cli.sendCommand(
                new CliCommand(
                        CommandType.VOLUMES_LIST,
                        listVolumesSpec.toCommand()
                ),
                CommandResult::lines
        );
        if (ids.isEmpty()) {
            return Stream.empty();
        } else {
            return parent.inspect(ids);
        }
    }
}
