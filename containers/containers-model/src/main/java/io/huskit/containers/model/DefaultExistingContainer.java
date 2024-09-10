package io.huskit.containers.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class DefaultExistingContainer implements ExistingContainer {

    String id;
    String containerKey;
    Long createdAt;
    Map<String, String> labels;

    @Override
    public boolean isExpired(Duration cleanupAfter) {
        var cleanupAfterMillis = cleanupAfter.toMillis();
        return cleanupAfterMillis > 0 && System.currentTimeMillis() - createdAt > cleanupAfterMillis;
    }
}
