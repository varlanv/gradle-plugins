package io.huskit.containers.model.id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public final class DefaultContainersIds implements ContainersIds {

    Set<ContainerId> set;
}
