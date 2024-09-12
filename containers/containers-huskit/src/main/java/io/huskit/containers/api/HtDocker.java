package io.huskit.containers.api;

import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.cli.HtCliDocker;

public interface HtDocker {

    HtListContainers listContainers();

    HtLogs logs(CharSequence containerId);

    HtRun run(HtDockerImageName dockerImageName);

    HtRun run(CharSequence dockerImageName);

    static HtCliDocker cli() {
        return new HtCliDocker();
    }
}
