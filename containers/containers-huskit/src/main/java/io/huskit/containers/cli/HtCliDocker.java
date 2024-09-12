package io.huskit.containers.cli;

import io.huskit.containers.HtDefaultDockerImageName;
import io.huskit.containers.api.HtDocker;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.HtLogs;
import io.huskit.containers.api.logs.HtCliLogs;
import io.huskit.containers.api.logs.LookFor;
import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgs;
import io.huskit.containers.api.run.HtRun;

public class HtCliDocker implements HtDocker {

    HtCli cli = new HtCli();

    @Override
    public HtListContainers listContainers() {
        return new HtCliListCtrs(cli, HtListContainersArgs.empty());
    }

    @Override
    public HtLogs logs(CharSequence containerId) {
        return new HtCliLogs(cli, containerId.toString(), LookFor.nothing());
    }

    @Override
    public HtRun run(HtDockerImageName dockerImageName) {
        return new HtCliRun(cli, dockerImageName, null);
    }

    @Override
    public HtRun run(CharSequence dockerImageName) {
        return new HtCliRun(cli, new HtDefaultDockerImageName(dockerImageName.toString()), null);
    }
}
