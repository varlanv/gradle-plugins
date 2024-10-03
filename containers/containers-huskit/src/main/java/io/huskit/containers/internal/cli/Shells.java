package io.huskit.containers.internal.cli;

import io.huskit.common.Os;
import io.huskit.common.Volatile;
import io.huskit.containers.api.cli.ShellType;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Shells {

    private static final Map<ShellType, Function<ShellPickArg, Shell>> SHELLS = new EnumMap<>(
            Arrays.stream(ShellType.values())
                    .collect(
                            Collectors.toMap(
                                    Function.identity(),
                                    shellType -> CliShell::new
                            )
                    )
    );
    private static final Volatile<Shell> DEFAULT_SHELL = Volatile.of();

    public Shell pickDefault(ShellPickArg arg) {
        return DEFAULT_SHELL.syncSetOrGet(() -> {
            if (Os.WINDOWS.isCurrent()) {
                return SHELLS.get(ShellType.POWERSHELL).apply(arg);
            } else if (Os.LINUX.isCurrent() || Os.MAC.isCurrent()) {
                return SHELLS.get(ShellType.SH).apply(arg);
            } else {
                throw new IllegalStateException("Could not determine default shell type out of available options: " + SHELLS.keySet());
            }
        });
    }

    public Shell take(ShellPickArg arg) {
        return Optional.ofNullable(SHELLS.get(arg.shellType()))
                .map(fn -> fn.apply(arg))
                .orElseGet(() -> pickDefault(arg));
    }
}
