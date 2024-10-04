package io.huskit.containers.internal.cli;

import io.huskit.common.HtStrings;
import io.huskit.containers.api.cli.HtArg;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class FindIdsCommand {

    List<HtArg> cmdArgs;

    public List<String> list() {
        var staticArgsAmount = 4;
        var command = new ArrayList<String>(staticArgsAmount + cmdArgs.size());
        command.add("docker");
        command.add("ps");
        cmdArgs.forEach(arg -> {
            command.add(arg.name());
            command.addAll(arg.values());
        });
        command.add("--format");
        command.add(HtStrings.doubleQuote("{{.ID}}"));
        return command;
    }
}
