package io.huskit.containers.model.port;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FixedContainerPort implements ContainerPort {

    int number;
}
