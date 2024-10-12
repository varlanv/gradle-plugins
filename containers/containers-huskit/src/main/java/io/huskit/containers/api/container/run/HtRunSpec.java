package io.huskit.containers.api.container.run;

import java.time.Duration;
import java.util.Map;

public interface HtRunSpec extends HtCreateSpec {

    @Override
    HtRunSpec withLabels(Map<String, ?> labels);

    @Override
    HtRunSpec withEnv(Map<String, ?> env);

    @Override
    HtRunSpec withPortBinding(Number hostPort, Number containerPort);

    @Override
    HtRunSpec withPortBindings(Map<? extends Number, ? extends Number> portBindings);

    @Override
    HtRunSpec withCommand(CharSequence command, Object... args);

    @Override
    HtRunSpec withCommand(CharSequence command, Iterable<?> args);

    @Override
    HtRunSpec withRemove();

    HtRunSpec withLookFor(CharSequence text, Duration timeout);

    default HtRunSpec withLookFor(CharSequence text) {
        return withLookFor(text, Duration.ZERO);
    }
}
