package io.huskit.common.port;

import java.util.Optional;

public interface ContainerPort {

    Integer hostValue();

    Optional<Integer> containerValue();

    Boolean isFixed();
}
