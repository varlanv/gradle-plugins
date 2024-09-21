package io.huskit.containers.api;

import io.huskit.containers.api.list.HtListContainers;
import io.huskit.containers.api.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.logs.HtLogs;
import io.huskit.containers.api.rm.HtRm;
import io.huskit.containers.api.run.HtRun;
import io.huskit.containers.api.run.HtRunSpec;

import java.util.List;
import java.util.function.Consumer;

public interface HtContainers {

    HtListContainers list();

    HtListContainers list(Consumer<HtListContainersArgsSpec> args);

    HtLogs logs(CharSequence containerId);

    HtRun run(CharSequence dockerImageName);

    HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec);

    HtRm remove(CharSequence... containerIds);

    <T extends CharSequence> HtRm remove(List<T> containerIds);
}
