package io.huskit.containers.integration;

import io.huskit.common.Log;
import io.huskit.common.Mutable;
import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class ContainerHash {

    Mutable<Log> log;
    List<Object> hashObjects = new ArrayList<>();
    MemoizedSupplier<String> hashSupplier = MemoizedSupplier.of(this::initHash);

    public String compute() {
        var hash = hashSupplier.get();
        log.require().debug(() -> "Computed hash [%s] based on values %s".formatted(hash, hashObjects));
        return hash;
    }

    public ContainerHash add(Object object) {
        hashObjects.add(object);
        return this;
    }

    private String initHash() {
        return String.valueOf(Objects.hashCode(hashObjects));
    }
}
