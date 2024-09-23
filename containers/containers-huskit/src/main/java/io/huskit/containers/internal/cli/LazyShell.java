package io.huskit.containers.internal.cli;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.function.ThrowingSupplier;
import io.huskit.containers.api.cli.ShellType;

public class LazyShell implements Shell {

    MemoizedSupplier<Shell> shellDelegate;

    public LazyShell(ThrowingSupplier<Shell> shellDelegate) {
        this.shellDelegate = new MemoizedSupplier<>(shellDelegate);
    }

    @Override
    public void write(String command) {
        shellDelegate.get().write(command);
    }

    @Override
    public ShellType type() {
        return shellDelegate.get().type();
    }

    @Override
    public long pid() {
        if (!shellDelegate.isInitialized()) {
            throw new IllegalStateException("Cannot get PID of uninitialized shell");
        }
        return shellDelegate.get().pid();
    }

    @Override
    public String outLine() {
        return shellDelegate.get().outLine();
    }

    @Override
    public void close() {
        if (shellDelegate.isInitialized()) {
            shellDelegate.get().close();
        }
    }
}
