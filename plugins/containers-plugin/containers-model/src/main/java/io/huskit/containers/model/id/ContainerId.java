package io.huskit.containers.model.id;

import io.huskit.common.function.MemoizedSupplier;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

public interface ContainerId {

    String json();

    static ContainerId of(Map<String, Object> map) {
        return new MemoizedSupplier<>(() -> new TreeMap<>(map).toString())::get;
    }

    static ContainerId of(Supplier<Map<String, Object>> mapSupplier) {
        return new MemoizedSupplier<>(() -> new TreeMap<>(mapSupplier.get()).toString())::get;
    }
}
