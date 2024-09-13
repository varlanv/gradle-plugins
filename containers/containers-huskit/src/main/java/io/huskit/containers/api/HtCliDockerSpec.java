package io.huskit.containers.api;

public interface HtCliDockerSpec {

    CliRecorder recorder();

    HtCliDockerSpec withCliRecorder(CliRecorder recorder);
}
