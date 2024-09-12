package io.huskit.containers.api;

import io.huskit.containers.cli.HtCliDocker;
import io.huskit.containers.api.ps.HtPs;
import io.huskit.containers.api.run.HtRun;

public interface HtDocker {

    HtPs listContainers();

    HtLogs logs(CharSequence containerId);

    HtRun run(HtDockerImageName dockerImageName);

    HtRun run(CharSequence dockerImageName);

    static HtCliDocker cli() {
        return new HtCliDocker();
    }
}
