package io.huskit.containers.model.id;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class DefaultContainersIds implements ContainersIds {

    private final Set<ContainerId> containerIds;

    @Override
    public Set<ContainerId> set() {
        return containerIds;
    }
}
