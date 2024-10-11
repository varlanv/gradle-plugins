package io.huskit.containers.api.container;

import io.huskit.containers.api.container.exec.HtExec;
import io.huskit.containers.api.container.run.HtRmSpec;
import io.huskit.containers.api.container.list.HtListContainers;
import io.huskit.containers.api.container.list.arg.HtListContainersArgsSpec;
import io.huskit.containers.api.container.logs.HtLogs;
import io.huskit.containers.api.container.rm.HtRm;
import io.huskit.containers.api.container.run.HtRun;
import io.huskit.containers.api.container.run.HtRunSpec;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface HtContainers {

    HtListContainers list();

    HtListContainers list(Consumer<HtListContainersArgsSpec> argsAction);

    Stream<HtContainer> inspect(Iterable<? extends CharSequence> containerIds);

    HtContainer inspect(CharSequence containerId);

    HtLogs logs(CharSequence containerId);

    HtRun run(CharSequence dockerImageName);

    HtRun run(CharSequence dockerImageName, Consumer<HtRunSpec> spec);

    HtExec execInContainer(CharSequence containerId, CharSequence command, Iterable<? extends CharSequence> args);

    HtExec execInContainer(CharSequence containerId, CharSequence command);

    HtRm remove(CharSequence... containerIds);

    HtRm remove(CharSequence containerId, Consumer<HtRmSpec> specAction);

    <T extends CharSequence> HtRm remove(Collection<T> containerIds, Consumer<HtRmSpec> specAction);
}
