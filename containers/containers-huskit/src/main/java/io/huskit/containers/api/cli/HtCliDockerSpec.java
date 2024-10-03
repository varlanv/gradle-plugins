package io.huskit.containers.api.cli;

public interface HtCliDockerSpec extends HtDockerSpec {

    HtCliDockerSpec withCliRecorder(CliRecorder recorder);

    HtCliDockerSpec withShell(ShellType shell);

    HtCliDockerSpec withForwardStderr(Boolean forwardStderr);

    HtCliDockerSpec withForwardStdout(Boolean forwardStdout);

    @Override
    HtCliDockerSpec withCleanOnClose(Boolean cleanOnClose);

    @Override
    HtCliDockerSpec withImagePrefix(CharSequence imagePrefix);
}
