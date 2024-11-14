package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import io.huskit.common.Volatile;
import io.huskit.containers.api.docker.HtDocker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@RequiredArgsConstructor
public class DefDockerClientSpec implements DockerClientSpec {

    @NonFinal
    @Nullable
    HtServiceContainer parent;
    @Getter
    Mutable<HtDocker> docker = Volatile.of();

    @Override
    public HtServiceContainer withDocker(HtDocker docker) {
        this.docker.set(docker);
        return Objects.requireNonNull(parent);
    }

    HtServiceContainer setParent(HtServiceContainer parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Parent already set");
        }
        this.parent = parent;
        return parent;
    }
}
