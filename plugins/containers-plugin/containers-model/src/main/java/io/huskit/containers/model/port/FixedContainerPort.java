package io.huskit.containers.model.port;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class FixedContainerPort implements ContainerPort {

    int number;
}
