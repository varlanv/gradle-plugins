package io.huskit.containers.api.container.run;

import java.util.Map;

public interface HtCreateSpec {

    HtCreateSpec withLabels(Map<String, ?> labels);

    HtCreateSpec withEnv(Map<String, ?> env);

    HtCreateSpec withRemove();

    HtCreateSpec withPortBinding(Number hostPort, Number containerPort);

    HtCreateSpec withPortBindings(Map<? extends Number, ? extends Number> portBindings);

    HtCreateSpec withCommand(CharSequence command, Object... args);

    HtCreateSpec withCommand(CharSequence command, Iterable<?> args);
}
