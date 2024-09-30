package io.huskit.containers.api;

import io.huskit.common.Volatile;
import io.huskit.containers.api.cli.CliRecorder;
import io.huskit.containers.api.cli.HtCliDckrSpec;
import io.huskit.containers.api.cli.HtCliDocker;
import io.huskit.containers.api.cli.ShellType;
import io.huskit.containers.api.image.HtImages;
import io.huskit.containers.internal.cli.HtCli;
import io.huskit.containers.internal.cli.HtCliDckr;
import io.huskit.containers.internal.cli.Shells;

public interface HtDocker {

    HtDocker withCleanOnClose(Boolean cleanOnClose);

    HtContainers containers();

    HtImages images();

    static HtDocker anyClient() {
        return cli();
    }

    static HtCliDocker cli() {
        var dockerSpec = new HtCliDckrSpec(
                Volatile.of(CliRecorder.noop()),
                Volatile.of(false),
                Volatile.of(ShellType.DEFAULT)
        );
        return new HtCliDckr(
                new HtCli(dockerSpec, new Shells()),
                dockerSpec
        );
    }
}
