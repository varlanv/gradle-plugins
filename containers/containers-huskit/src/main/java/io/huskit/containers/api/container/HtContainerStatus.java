package io.huskit.containers.api.container;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum HtContainerStatus {

    CREATED,
    RUNNING,
    PAUSED,
    RESTARTING,
    REMOVING,
    EXITED,
    DEAD;

    String label;
    private static final Map<String, HtContainerStatus> labelToStatus = Arrays.stream(values())
        .collect(
            Collectors.toMap(
                HtContainerStatus::label,
                Function.identity()
            ));

    HtContainerStatus() {
        this.label = name().toLowerCase();
    }

    public static HtContainerStatus fromLabel(String label) {
        var status = labelToStatus.get(label);
        if (status == null) {
            throw new IllegalArgumentException("Unknown container status: " + label);
        }
        return status;
    }
}
