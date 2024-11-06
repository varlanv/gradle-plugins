package io.huskit.containers.api.docker;

import io.huskit.containers.api.container.HtContainers;
import io.huskit.containers.api.image.HtImages;
import io.huskit.containers.api.volume.HtVolumes;
import io.huskit.containers.cli.*;
import io.huskit.containers.http.HtHttpDckr;
import io.huskit.containers.http.HtHttpDocker;

public interface HtDocker {

    HtDocker withCleanOnClose(Boolean cleanOnClose);

    HtContainers containers();

    HtImages images();

    HtVolumes volumes();

    static HtDocker anyClient() {
        return http();
    }

    static HtCliDocker cli() {
        var dockerSpec = new HtCliDckrSpec().withForwardStdout(true).withForwardStderr(true);
        return new HtCliDckr(
            new HtCli(dockerSpec, new Shells()),
            dockerSpec
        );
    }

    static HtHttpDocker http() {
        return new HtHttpDckr().withCleanOnClose(true);
    }
}
