package io.huskit.containers.api.cli;

public interface HtCliDockerSpec {

    HtCliDockerSpec withCliRecorder(CliRecorder recorder);

    HtCliDockerSpec withCleanOnClose(Boolean cleanOnClose);

    HtCliDockerSpec withShell(ShellType shell);
}
