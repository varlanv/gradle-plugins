package io.huskit.containers.api.cli;

public interface HtCliDockerSpec extends HtDockerSpec {

    HtCliDockerSpec withCliRecorder(CliRecorder recorder);

    HtCliDockerSpec withShell(ShellType shell);
}
