package io.huskit.containers.integration.mongo;

import io.huskit.common.function.MemoizedSupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ContainerHash {

    List<Object> hashObjects = new ArrayList<>();
    MemoizedSupplier<String> hashSupplier = new MemoizedSupplier<>(this::initHash);

    public String compute() {
        var hash = hashSupplier.get();
        System.out.printf("Computed hash [%s] based on values %s%n", hash, hashObjects);
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
