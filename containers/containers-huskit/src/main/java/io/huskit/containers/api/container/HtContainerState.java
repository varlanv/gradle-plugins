package io.huskit.containers.api.container;

import java.time.Instant;

public interface HtContainerState {

    HtContainerStatus status();

    Boolean running();

    Boolean paused();

    Boolean restarting();

    Boolean oomKilled();

    Boolean dead();

    Integer pid();

    Integer exitCode();

    Instant startedAt();

    Instant finishedAt();

    String error();
}
