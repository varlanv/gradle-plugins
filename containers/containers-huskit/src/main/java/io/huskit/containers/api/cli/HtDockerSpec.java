package io.huskit.containers.api.cli;

public interface HtDockerSpec {

    HtDockerSpec withCleanOnClose(Boolean cleanOnClose);

    HtDockerSpec withImagePrefix(CharSequence imagePrefix);
}
