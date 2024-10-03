package io.huskit.containers.api.run;

import java.time.Duration;
import java.util.Map;

public interface HtRunSpec {

    HtRunSpec withLabels(Map<String, ?> labels);

    HtRunSpec withEnv(Map<String, ?> labels);

    HtRunSpec withRemove();

    HtRunSpec withPortBinding(Number hostPort, Number containerPort);

    HtRunSpec withPortBindings(Map<? extends Number, ? extends Number> portBindings);

    HtRunSpec withCommand(CharSequence command, Object... args);

    HtRunSpec withLookFor(CharSequence text, Duration timeout);

    default HtRunSpec withLookFor(CharSequence text) {
        return withLookFor(text, Duration.ZERO);
    }
}
