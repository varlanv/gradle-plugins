package io.huskit.containers.api;

import io.huskit.containers.api.cli.CommandType;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.internal.HtJson;
import io.huskit.containers.internal.cli.CliCommand;
import io.huskit.containers.internal.cli.CommandResult;
import io.huskit.containers.internal.cli.HtCli;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtCliVolumes implements HtVolumes {

    HtCli cli;
    HtCliDckrSpec dockerSpec;

    @Override
    public HtListVolumes list() {
        return new HtCliListVolumes(
                cli,
                new HtCliListVolumesSpec(),
                this
        );
    }

    @Override
    public HtListVolumes list(Consumer<HtListVolumesSpec> action) {
        var listVolumesSpec = new HtCliListVolumesSpec();
        action.accept(listVolumesSpec);
        return new HtCliListVolumes(
                cli,
                listVolumesSpec,
                this
        );
    }

    @Override
    public HtCreateVolume create() {
        return new HtCliCreateVolume(
                cli,
                new HtCliCreateVolumeSpec()
        );
    }

    @Override
    public HtCreateVolume create(CharSequence volumeId) {
        return new HtCliCreateVolume(
                cli,
                new HtCliCreateVolumeSpec(volumeId)
        );
    }

    @Override
    public HtCreateVolume create(Consumer<HtCreateVolumeSpec> action) {
        var htCliCreateVolumeSpec = new HtCliCreateVolumeSpec();
        action.accept(htCliCreateVolumeSpec);
        return new HtCliCreateVolume(
                cli,
                htCliCreateVolumeSpec
        );
    }

    @Override
    public HtCreateVolume create(CharSequence volumeId, Consumer<HtCreateVolumeSpec> action) {
        var htCliCreateVolumeSpec = new HtCliCreateVolumeSpec(volumeId);
        action.accept(htCliCreateVolumeSpec);
        return new HtCliCreateVolume(
                cli,
                htCliCreateVolumeSpec
        );
    }

    @Override
    public HtRemoveVolumes rm(CharSequence volumeId, Boolean force) {
        return new HtCliRemoveVolumes(
                cli,
                new HtCliRemoveVolumesSpec(
                        force,
                        Collections.singletonList(volumeId)
                )
        );
    }

    @Override
    public <T extends CharSequence> HtRemoveVolumes rm(Iterable<T> imageRefs, Boolean force) {
        return new HtCliRemoveVolumes(
                cli,
                new HtCliRemoveVolumesSpec(
                        force,
                        imageRefs
                )
        );
    }

    @Override
    public HtPruneVolumes prune(Consumer<HtPruneVolumesSpec> action) {
        var pruneVolumesSpec = new HtCliPruneVolumesSpec();
        action.accept(pruneVolumesSpec);
        return new HtCliPruneVolumes(
                cli,
                pruneVolumesSpec
        );
    }

    @Override
    public HtPruneVolumes prune() {
        return new HtCliPruneVolumes(
                cli,
                new HtCliPruneVolumesSpec()
        );
    }

    @Override
    public HtVolumeView inspect(CharSequence volumeId) {
        return new JsonHtVolumeView(
                volumeId.toString(),
                HtJson.toMap(
                        cli.sendCommand(
                                new CliCommand(
                                        CommandType.VOLUMES_INSPECT,
                                        new HtCliInspectVolumesSpec(
                                                Collections.singletonList(
                                                        volumeId
                                                )
                                        ).toCommand()
                                ),
                                CommandResult::singleLine
                        )
                )
        );
    }

    @Override
    public <T extends CharSequence> Stream<HtVolumeView> inspect(Iterable<T> volumeIds) {
        var jsons = cli.sendCommand(
                new CliCommand(
                        CommandType.VOLUMES_INSPECT,
                        new HtCliInspectVolumesSpec(volumeIds).toCommand()
                ),
                CommandResult::lines
        );
        var ids = new ArrayList<String>();
        for (var volumeId : volumeIds) {
            ids.add(volumeId.toString());
        }
        var result = new ArrayList<HtVolumeView>(jsons.size());
        for (var idx = 0; idx < jsons.size(); idx++) {
            result.add(
                    new JsonHtVolumeView(
                            ids.get(idx),
                            HtJson.toMap(jsons.get(idx))
                    )
            );
        }
        return result.stream();
    }
}
