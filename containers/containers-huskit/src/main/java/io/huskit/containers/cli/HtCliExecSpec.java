package io.huskit.containers.cli;

import io.huskit.common.HtStrings;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
class HtCliExecSpec {

    String containerId;
    String cmd;
    List<String> args;

    public List<String> toCommand() {
        var command = new ArrayList<String>(4 + args.size());
        command.add("docker");
        command.add("exec");
        command.add(containerId);
        command.add(HtStrings.doubleQuote(cmd));
        for (var arg : args) {
            command.add(HtStrings.doubleQuote(arg));
        }
        return command;
    }
}
