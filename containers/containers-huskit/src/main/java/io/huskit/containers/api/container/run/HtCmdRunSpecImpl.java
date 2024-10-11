package io.huskit.containers.api.container.run;

import io.huskit.common.Mutable;
import io.huskit.common.HtStrings;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.model.CommandType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@With
@RequiredArgsConstructor
public class HtCmdRunSpecImpl implements HtRunSpec {

    HtImgName imgName;
    Mutable<Map<String, String>> labels = Mutable.of();
    Mutable<Map<String, String>> env = Mutable.of();
    Mutable<Map<Integer, Integer>> ports = Mutable.of();
    Mutable<Boolean> remove = Mutable.of(false);
    Mutable<RunCommand> command = Mutable.of();
    @Getter
    Mutable<String> lookFor = Mutable.of();
    Mutable<Duration> timeout = Mutable.of(Duration.ZERO);

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
    public HtRunSpec withPortBinding(Number hostPort, Number containerPort) {
        return withPortBindings(Collections.singletonMap(hostPort, containerPort));
    }

    @Override
    public HtRunSpec withPortBindings(Map<? extends Number, ? extends Number> portBindings) {
        this.ports.set(
                Collections.unmodifiableMap(
                        portBindings.entrySet().stream()
                                .collect(
                                        Collectors.toMap(
                                                entry -> entry.getKey().intValue(),
                                                entry -> entry.getValue().intValue()
                                        )
                                )
                )
        );
        return this;
    }

    @Override
    public HtCmdRunSpecImpl withCommand(CharSequence command, Object... args) {
        return withCommand(command, Arrays.asList(args));
    }

    @Override
    public HtCmdRunSpecImpl withCommand(CharSequence command, Iterable<?> args) {
        var argsList = new ArrayList<String>(4);
        for (var arg : args) {
            argsList.add(arg.toString());
        }
        this.command.set(
                new RunCommand(
                        command.toString(),
                        argsList
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

    public List<String> toCommand() {
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
            processCmd.add(HtStrings.doubleQuotedParam(k, v));
        }));
        env.ifPresent(envMap -> envMap.forEach((k, v) -> {
            processCmd.add("-e");
            processCmd.add(HtStrings.doubleQuotedParam(k, v));
        }));
        ports.ifPresent(portMap -> portMap.forEach((k, v) -> {
            processCmd.add("-p");
            processCmd.add(HtStrings.doubleQuote(k + ":" + v));
        }));

        processCmd.add(imgName.reference());
        command.ifPresent(runCmd -> {
            processCmd.add(runCmd.command());
            for (var arg : runCmd.args()) {
                processCmd.add(HtStrings.doubleQuoteIfNotAlready(arg));
            }
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
