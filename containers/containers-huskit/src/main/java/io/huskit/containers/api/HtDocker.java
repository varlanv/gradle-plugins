package io.huskit.containers.api;

import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.cli.HtCli;
import io.huskit.containers.cli.HtCliDocker;

import java.util.List;

public interface HtDocker {

    HtListContainers listContainers();

    HtLogs logs(CharSequence containerId);

    HtRun run(HtDockerImageName dockerImageName);

    HtRun run(CharSequence dockerImageName);

    HtRm remove(CharSequence... containerIds);

    <T extends CharSequence> HtRm remove(List<T> containerIds);

    static HtCliDocker cli() {
        return new HtCliDocker(new HtCli());
    }
}
