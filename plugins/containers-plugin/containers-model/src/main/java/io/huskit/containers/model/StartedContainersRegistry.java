package io.huskit.containers.model;

import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class StartedContainersRegistry {

    Log log;
    ConcurrentMap<String, Value<StartedContainer>> startedContainersById = new ConcurrentHashMap<>();
    ConcurrentMap<String, Value<StartedContainer>> startedContainersBySourceAndId = new ConcurrentHashMap<>();
    Collection<StartedContainer> allStartedContainers = new ConcurrentLinkedQueue<>();
    KnownDockerContainers knownDockerContainers;

    public Stream<StartedContainer> all() {
        return allStartedContainers.stream();
    }

    public StartedContainer getOrStart(RequestedContainer requestedContainer) {
        var key = requestedContainer.id().json();
        if (requestedContainer.reuseOptions().enabled()) {
            return getStartedContainerInternal(startedContainersById, key, requestedContainer);
        } else {
            return getStartedContainerInternal(startedContainersBySourceAndId, key, requestedContainer);
        }
    }

    private StartedContainer getStartedContainerInternal(ConcurrentMap<String, Value<StartedContainer>> startedContainersByKey,
                                                         String key,
                                                         RequestedContainer requestedContainer) {
        var startedContainerValue = startedContainersByKey.computeIfAbsent(key, k -> new Value<>());
        var result = startedContainerValue.ref;
        if (result == null) {
            try {
                startedContainerValue.lock.lock();
                result = startedContainerValue.ref;
                if (result == null) {
                    log.info("Container is not started yet, sync block is entered, key=[{}]", key);
                    result = knownDockerContainers.prepareForStart(requestedContainer).start();
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
        volatile T ref;
        Lock lock = new ReentrantLock();
    }
}
