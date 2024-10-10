package io.huskit.containers.model;

import io.huskit.containers.integration.ContainerSpec;
import io.huskit.containers.integration.HtStartedContainer;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StartedContainersRegistry {

    ConcurrentMap<String, Value<HtStartedContainer>> startedContainersById = new ConcurrentHashMap<>();
    Collection<HtStartedContainer> allStartedContainers = new ConcurrentLinkedQueue<>();

    public Stream<HtStartedContainer> all() {
        return allStartedContainers.stream();
    }

    public HtStartedContainer getOrStart(ContainerSpec containerSpec, Function<ContainerSpec, HtStartedContainer> fn) {
        var key = containerSpec.hash();
        return getStartedContainerInternal(startedContainersById, key, containerSpec, fn);
    }

    private HtStartedContainer getStartedContainerInternal(ConcurrentMap<String, Value<HtStartedContainer>> startedContainersByKey,
                                                           String key,
                                                           ContainerSpec containerSpec,
                                                           Function<ContainerSpec, HtStartedContainer> fn) {
        var startedContainerValue = startedContainersByKey.computeIfAbsent(key, k -> new Value<>());
        var result = startedContainerValue.ref;
        if (result == null) {
            try {
                startedContainerValue.lock.lock();
                result = startedContainerValue.ref;
                if (result == null) {
                    result = fn.apply(containerSpec);
                    allStartedContainers.add(result);
                    startedContainerValue.ref = result;
                }
            } finally {
                startedContainerValue.lock.unlock();
            }
        }
        return Objects.requireNonNull(result);
    }

    @RequiredArgsConstructor
    private static class Value<T> {

        @NonFinal
        @Nullable
        volatile T ref;
        Lock lock = new ReentrantLock();
    }
}
