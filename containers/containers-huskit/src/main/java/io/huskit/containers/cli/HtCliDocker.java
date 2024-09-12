package io.huskit.containers.cli;

import io.huskit.containers.HtDefaultDockerImageName;
import io.huskit.containers.api.HtDocker;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.HtLogs;
import io.huskit.containers.api.logs.HtCliLogs;
import io.huskit.containers.api.logs.LookFor;
import io.huskit.containers.api.ps.HtPs;
import io.huskit.containers.api.ps.arg.HtPsArgs;
import io.huskit.containers.api.run.HtRun;

public class HtCliDocker implements HtDocker {

    HtCli cli = new HtCli();

    @Override
    public HtPs listContainers() {
        return new HtCliPs(cli, HtPsArgs.empty());
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
