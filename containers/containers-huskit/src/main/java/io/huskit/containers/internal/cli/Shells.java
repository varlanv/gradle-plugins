package io.huskit.containers.internal.cli;

import io.huskit.common.Environment;
import io.huskit.common.Volatile;
import io.huskit.containers.api.ShellType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.huskit.common.Environment.UNIX;
import static io.huskit.common.Environment.WINDOWS;

public class Shells {

    private static final Map<ShellType, Supplier<Shell>> SHELLS = new EnumMap<>(Map.of(
            ShellType.SH, () -> new Sh("/bin/sh"),
            ShellType.POWERSHELL, () -> new PowerShell("powershell"),
            ShellType.GITBASH, () -> new GitBash("C:\\Program Files\\Git\\bin\\bash.exe")
    ));
    private static final Volatile<Shell> DEFAULT_SHELL = Volatile.of();

    public Shell pickDefault() {
        return DEFAULT_SHELL.syncSetOrGet(() -> {
            if (Environment.is(WINDOWS)) {
                return SHELLS.get(ShellType.POWERSHELL).get();
            } else if (Environment.is(UNIX)) {
                return SHELLS.get(ShellType.SH).get();
            } else {
                throw new IllegalStateException("Could not determine shell type");
            }
        });
    }

    public Shell take(ShellType shellType) {
        return Optional.ofNullable(SHELLS.get(shellType))
                .map(Supplier::get)
                .orElseGet(this::pickDefault);
    }
}
