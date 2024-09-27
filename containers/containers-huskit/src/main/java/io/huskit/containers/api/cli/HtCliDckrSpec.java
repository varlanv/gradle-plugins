package io.huskit.containers.api.cli;

import io.huskit.common.Volatile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HtCliDckrSpec implements HtCliDockerSpec {

    Volatile<CliRecorder> recorder;
    Volatile<Boolean> cleanOnClose;
    Volatile<ShellType> shell;

    public HtCliDckrSpec(HtCliDckrSpec another) {
        this(
                Volatile.of(another.recorder()),
                Volatile.of(another.isCleanOnClose()),
                Volatile.of(another.shell())
        );
    }

    @Override
    public HtCliDockerSpec withCliRecorder(CliRecorder recorder) {
        this.recorder.set(recorder);
        return this;
    }

    @Override
    public HtCliDockerSpec withShell(ShellType shell) {
        this.shell.set(shell);
        return this;
    }

    public CliRecorder recorder() {
        return recorder.require();
    }

    public Boolean isCleanOnClose() {
        return cleanOnClose.require();
    }

    public ShellType shell() {
        return shell.require();
    }

    @Override
    public HtCliDckrSpec withCleanOnClose(Boolean cleanOnClose) {
        this.cleanOnClose.set(cleanOnClose);
        return this;
    }
}
