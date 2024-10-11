package io.huskit.containers.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShellPickArg {

    ShellType shellType;
    Boolean forwardStderr;
    Boolean forwardStdout;
}
