package io.huskit.containers.internal.cli;

import io.huskit.containers.api.cli.ShellType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShellPickArg {

    ShellType shellType;
    Boolean forwardStderr;
    Boolean forwardStdout;
}
