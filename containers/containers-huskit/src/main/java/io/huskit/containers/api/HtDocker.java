package io.huskit.containers.api;

import io.huskit.common.Volatile;
import io.huskit.containers.internal.cli.HtCli;
import io.huskit.containers.internal.cli.HtCliDckr;

public interface HtDocker {

    HtContainers containers();

    static HtCliDckr cli() {
        var dockerSpec = new HtCliDckrSpec(
                Volatile.of(CliRecorder.noop()),
                Volatile.of(false),
                Volatile.of(Shell.DEFAULT)
        );
        return new HtCliDckr(
                new HtCli(dockerSpec),
                dockerSpec
        );
    }
}
