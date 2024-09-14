package io.huskit.containers.api;

import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.internal.cli.HtCli;
import io.huskit.containers.internal.cli.HtCliDckr;

import java.util.List;

public interface HtDocker {

    HtListContainers listContainers();

    HtLogs logs(CharSequence containerId);

    HtRun run(HtDockerImageName dockerImageName);

    HtRun run(CharSequence dockerImageName);

    HtRm remove(CharSequence... containerIds);

    <T extends CharSequence> HtRm remove(List<T> containerIds);

    static HtCliDckr cli() {
        var recorder = CliRecorder.noop();
        return new HtCliDckr(
                new HtCli(recorder),
                new HtCliDckrSpec(recorder)
        );
    }
}
