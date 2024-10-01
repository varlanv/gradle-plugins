package io.huskit.containers.api;

import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.logs.HtLogs;
import io.huskit.containers.api.rm.HtRm;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.api.run.HtRunSpec;
import io.huskit.containers.internal.cli.HtCliRmSpec;

import java.util.Collection;
import java.util.function.Consumer;

public interface HtContainers {

    HtListContainers list();

    HtListContainers list(Consumer<HtListContainersArgsSpec> argsAction);

    HtLogs logs(CharSequence containerId);

    HtRun run(CharSequence dockerImageName);

    HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec);

    HtRm remove(CharSequence... containerIds);

    HtRm remove(CharSequence containerId, Consumer<HtCliRmSpec> specAction);

    <T extends CharSequence> HtRm remove(Collection<T> containerIds, Consumer<HtCliRmSpec> specAction);
}
