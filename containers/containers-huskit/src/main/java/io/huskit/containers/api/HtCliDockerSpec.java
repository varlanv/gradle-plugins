package io.huskit.containers.api;

public interface HtCliDockerSpec {

    HtCliDockerSpec withCliRecorder(CliRecorder recorder);

    HtCliDockerSpec withCleanOnClose(Boolean cleanOnClose);

    HtCliDockerSpec withShell(Shell shell);
}
