package io.huskit.containers.internal.cli;

import io.huskit.common.Os;
import io.huskit.common.Volatile;
import io.huskit.containers.api.ShellType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class Shells {

    private static final Map<ShellType, Supplier<Shell>> SHELLS = new EnumMap<>(Map.of(
            ShellType.SH, Sh::new,
            ShellType.POWERSHELL, PowerShell::new,
            ShellType.BASH, Bash::new,
            ShellType.CMD, Cmd::new
    ));
    private static final Volatile<Shell> DEFAULT_SHELL = Volatile.of();

    public Shell pickDefault() {
        return DEFAULT_SHELL.syncSetOrGet(() -> {
            if (Os.WINDOWS.isCurrent()) {
                return SHELLS.get(ShellType.POWERSHELL).get();
            } else if (Os.LINUX.isCurrent() || Os.MAC.isCurrent()) {
                return SHELLS.get(ShellType.SH).get();
            } else {
                throw new IllegalStateException("Could not determine default shell type out of available options: " + SHELLS.keySet());
            }
        });
    }

    public Shell take(ShellType shellType) {
        return Optional.ofNullable(SHELLS.get(shellType))
                .map(Supplier::get)
                .orElseGet(this::pickDefault);
    }
}
