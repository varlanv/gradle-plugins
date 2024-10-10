package io.huskit.containers.api.cli;

import io.huskit.common.Volatile;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class HtCliDckrSpec implements HtCliDockerSpec {

    Volatile<CliRecorder> recorder;
    Volatile<Boolean> cleanOnClose;
    Volatile<ShellType> shell;
    Volatile<Boolean> forwardStderr;
    Volatile<Boolean> forwardStdout;
    Volatile<String> imagePrefix;

    public HtCliDckrSpec() {
        this(
                Volatile.of(CliRecorder.noop()),
                Volatile.of(false),
                Volatile.of(ShellType.DEFAULT),
                Volatile.of(false),
                Volatile.of(false),
                Volatile.of("")
        );
    }

    public HtCliDckrSpec(HtCliDckrSpec another) {
        this(
                Volatile.of(another.recorder()),
                Volatile.of(another.isCleanOnClose()),
                Volatile.of(another.shell()),
                Volatile.of(another.forwardStderr()),
                Volatile.of(another.forwardStdout()),
                Volatile.of(another.imagePrefix())
        );
    }

    @Override
    public HtCliDckrSpec withCliRecorder(CliRecorder recorder) {
        this.recorder.set(recorder);
        return this;
    }

    @Override
    public HtCliDckrSpec withShell(ShellType shell) {
        this.shell.set(shell);
        return this;
    }

    @Override
    public HtCliDckrSpec withForwardStderr(Boolean forwardStderr) {
        this.forwardStderr.set(forwardStderr);
        return this;
    }

    @Override
    public HtCliDckrSpec withForwardStdout(Boolean forwardStdout) {
        this.forwardStdout.set(forwardStdout);
        return this;
    }

    @Override
    public HtCliDckrSpec withCleanOnClose(Boolean cleanOnClose) {
        this.cleanOnClose.set(cleanOnClose);
        return this;
    }

    @Override
    public HtCliDckrSpec withImagePrefix(CharSequence imagePrefix) {
        this.imagePrefix.set(imagePrefix.toString());
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

    public Boolean forwardStderr() {
        return forwardStderr.require();
    }

    public Boolean forwardStdout() {
        return forwardStdout.require();
    }

    public String imagePrefix() {
        return imagePrefix.require();
    }
}
