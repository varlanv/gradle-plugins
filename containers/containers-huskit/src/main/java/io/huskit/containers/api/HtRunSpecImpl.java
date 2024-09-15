package io.huskit.containers.api;

import io.huskit.common.Volatile;
import io.huskit.containers.api.run.RunCommand;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@With
@Getter
@RequiredArgsConstructor
public class HtRunSpecImpl implements HtRunSpec {

    Volatile<Map<String, String>> labels = Volatile.of();
    Volatile<Map<String, String>> env = Volatile.of();
    Volatile<Boolean> remove = Volatile.of(false);
    Volatile<RunCommand> command = Volatile.of();
    Volatile<String> lookFor = Volatile.of();
    Volatile<Duration> timeout = Volatile.of(Duration.ZERO);

    @Override
    public HtRunSpec withLabels(Map<String, ?> labels) {
        this.labels.set(
                Collections.unmodifiableMap(
                        labels.entrySet().stream()
                                .collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> e.getValue().toString()
                                        )
                                )
                )
        );
        return this;
    }

    @Override
    public HtRunSpec withEnv(Map<String, ?> labels) {
        this.env.set(
                Collections.unmodifiableMap(
                        labels.entrySet().stream()
                                .collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> e.getValue().toString()
                                        )
                                )
                )
        );
        return this;
    }

    @Override
    public HtRunSpec withRemove() {
        this.remove.set(true);
        return this;
    }

    @Override
    public HtRunSpec withCommand(CharSequence command, Object... args) {
        this.command.set(
                new RunCommand(
                        command.toString(),
                        Stream.of(args)
                                .map(Object::toString)
                                .collect(Collectors.toList())
                )
        );
        return this;
    }

    @Override
    public HtRunSpec withLookFor(CharSequence text, Duration timeout) {
        this.lookFor.set(text.toString());
        this.timeout.set(timeout);
        return this;
    }
}
