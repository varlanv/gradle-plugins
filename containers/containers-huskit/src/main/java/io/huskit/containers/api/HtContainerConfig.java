package io.huskit.containers.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HtContainerConfig {

    Optional<String> entrypoint();

    Boolean attachStder();

    Boolean attachStdin();

    String hostname();

    Boolean openStdin();

    Optional<String> workingDir();

    Map<String, String> labels();

    Map<String, String> env();

    List<String> cmd();

    Boolean tty();
}
