package io.huskit.containers.api.run;

import io.huskit.common.Volatile;
import io.huskit.containers.api.HtDockerImageName;
import io.huskit.containers.api.cli.CommandType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@With
@RequiredArgsConstructor
public class HtCmdRunSpecImpl implements HtRunSpec {

    HtDockerImageName imgName;
    Volatile<Map<String, String>> labels = Volatile.of();
    Volatile<Map<String, String>> env = Volatile.of();
    Volatile<Boolean> remove = Volatile.of(false);
    Volatile<RunCommand> command = Volatile.of();
    @Getter
    Volatile<String> lookFor = Volatile.of();
    Volatile<Duration> timeout = Volatile.of(Duration.ZERO);

    @Override
    public HtCmdRunSpecImpl withLabels(Map<String, ?> labels) {
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
    public HtCmdRunSpecImpl withEnv(Map<String, ?> labels) {
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
    public HtCmdRunSpecImpl withRemove() {
        this.remove.set(true);
        return this;
    }

    @Override
    public HtCmdRunSpecImpl withCommand(CharSequence command, Object... args) {
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
    public HtCmdRunSpecImpl withLookFor(CharSequence text, Duration timeout) {
        this.lookFor.set(text.toString());
        this.timeout.set(timeout);
        return this;
    }

    public List<String> build() {
        var processCmd = new ArrayList<String>(4);
        processCmd.add("docker");
        processCmd.add("run");
        processCmd.add("-d");
        remove.ifPresent(rm -> {
            if (rm) {
                processCmd.add("--rm");
            }
        });
        labels.ifPresent(labelMap -> labelMap.forEach((k, v) -> {
            processCmd.add("--label");
            processCmd.add("\"" + k + "=" + v + "\"");
        }));
        env.ifPresent(envMap -> envMap.forEach((k, v) -> {
            processCmd.add("-e");
            processCmd.add("\"" + k + "=" + v + "\"");
        }));

        processCmd.add(imgName.id());
        command.ifPresent(runCmd -> {
            processCmd.add(runCmd.command());
            processCmd.addAll(runCmd.args());
        });
        return processCmd;
    }

    public CommandType commandType() {
        return lookFor.isPresent() ? CommandType.CONTAINERS_RUN_FOLLOW : CommandType.CONTAINERS_RUN;
    }

    public Duration timeout() {
        return timeout.or(Duration.ZERO);
    }
}
