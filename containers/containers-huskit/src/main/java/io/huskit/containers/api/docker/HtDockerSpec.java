package io.huskit.containers.api.docker;

public interface HtDockerSpec {

    HtDockerSpec withCleanOnClose(Boolean cleanOnClose);

    HtDockerSpec withImagePrefix(CharSequence imagePrefix);
}
