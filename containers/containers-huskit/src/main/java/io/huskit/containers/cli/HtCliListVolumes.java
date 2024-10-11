package io.huskit.containers.cli;

import io.huskit.containers.api.volume.HtListVolumes;
import io.huskit.containers.api.volume.HtVolumeView;
import io.huskit.containers.model.CommandType;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
class HtCliListVolumes implements HtListVolumes {

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
