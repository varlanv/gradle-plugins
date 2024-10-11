package io.huskit.containers.api.docker;

import io.huskit.containers.api.volume.HtVolumes;
import io.huskit.containers.api.container.HtContainers;
import io.huskit.containers.cli.HtCliDckrSpec;
import io.huskit.containers.cli.HtCliDocker;
import io.huskit.containers.api.image.HtImages;
import io.huskit.containers.cli.HtCli;
import io.huskit.containers.cli.HtCliDckr;
import io.huskit.containers.cli.Shells;

public interface HtDocker {

    HtDocker withCleanOnClose(Boolean cleanOnClose);

    HtContainers containers();

    HtImages images();

    HtVolumes volumes();

    static HtDocker anyClient() {
        return cli();
    }

    static HtCliDocker cli() {
        var dockerSpec = new HtCliDckrSpec().withForwardStdout(true).withForwardStderr(true);
        return new HtCliDckr(
                new HtCli(dockerSpec, new Shells()),
                dockerSpec
        );
    }
}
