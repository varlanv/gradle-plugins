package io.huskit.containers.api;

import java.time.Duration;
import java.util.Map;

public interface HtRunSpec {

    HtRunSpec withLabels(Map<String, ?> labels);

    HtRunSpec withEnv(Map<String, ?> labels);

    HtRunSpec withRemove();

    HtRunSpec withCommand(CharSequence command, Object... args);

    HtRunSpec withLookFor(CharSequence text, Duration timeout);

    default HtRunSpec withLookFor(CharSequence text) {
        return withLookFor(text, Duration.ZERO);
    }
}
