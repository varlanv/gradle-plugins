package io.huskit.containers.integration;

import java.util.List;

public interface ServicesSpec {

    List<ContainerSpec> containers();

    static ServicesSpec from(List<ContainerSpec> specs) {
        return () -> specs;
    }
}
