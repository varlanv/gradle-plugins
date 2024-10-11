package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import io.huskit.containers.model.HtConstants;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;

@RequiredArgsConstructor
public class JsonHtContainerState implements HtContainerState {

    Map<String, Object> source;

    @Override
    public HtContainerStatus status() {
        return HtContainerStatus.fromLabel(HtCollections.getFromMap("Status", source));
    }

    @Override
    public Boolean running() {
        return HtCollections.getFromMap("Running", source);
    }

    @Override
    public Boolean paused() {
        return HtCollections.getFromMap("Paused", source);
    }

    @Override
    public Boolean restarting() {
        return HtCollections.getFromMap("Restarting", source);
    }

    @Override
    public Boolean oomKilled() {
        return HtCollections.getFromMap("OOMKilled", source);
    }

    @Override
    public Boolean dead() {
        return HtCollections.getFromMap("Dead", source);
    }

    @Override
    public Integer pid() {
        return HtCollections.getFromMap("Pid", source);
    }

    @Override
    public Integer exitCode() {
        return HtCollections.getFromMap("ExitCode", source);
    }

    @Override
    public Instant startedAt() {
        var startedAt = Instant.parse(HtCollections.getFromMap("StartedAt", source));
        if (startedAt.toEpochMilli() == HtConstants.ZERO_INSTANT_MILLIS) {
            throw new IllegalStateException("Container is not yet started, consider checking container status before calling startedAt");
        }
        return startedAt;
    }

    @Override
    public Instant finishedAt() {
        var finishedAt = Instant.parse(HtCollections.getFromMap("FinishedAt", source));
        if (finishedAt.toEpochMilli() == HtConstants.ZERO_INSTANT_MILLIS) {
            throw new IllegalStateException("Container is not yet finished, consider checking container status before calling finishedAt");
        }
        return finishedAt;
    }

    @Override
    public String error() {
        return HtCollections.getFromMap("Error", source);
    }
}
