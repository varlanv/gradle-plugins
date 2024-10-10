package io.huskit.containers.api;

import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.cli.HtCliDocker;
import io.huskit.containers.api.image.HtImages;
import io.huskit.containers.internal.cli.HtCli;
import io.huskit.containers.internal.cli.HtCliDckr;
import io.huskit.containers.internal.cli.Shells;

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
