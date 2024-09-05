package io.huskit.gradle.containers.plugin.internal;

import io.huskit.containers.model.KnownDockerContainers;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.containers.model.started.StartedContainer;
import io.huskit.containers.model.started.StartedContainerInternal;
import io.huskit.containers.model.started.StartedContainersInternal;
import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor
public class DockerStartedContainersInternal implements StartedContainersInternal {

    Log log;
    ConcurrentMap<String, Value<StartedContainerInternal>> startedContainersById = new ConcurrentHashMap<>();
    ConcurrentMap<String, Value<StartedContainerInternal>> startedContainersBySourceAndId = new ConcurrentHashMap<>();
    Collection<StartedContainerInternal> allStartedContainers = new ConcurrentLinkedQueue<>();
    KnownDockerContainers knownDockerContainers;

    @Override
    public List<StartedContainer> list() {
        return new ArrayList<>(allStartedContainers);
    }

    @Override
    public StartedContainerInternal startOrCreateAndStart(RequestedContainer requestedContainer) {
        var key = requestedContainer.id().json();
        if (requestedContainer.reuseOptions().enabled()) {
            log.info("Container is reusable, key=[{}]", key);
            return getStartedContainerInternal(startedContainersById, key, requestedContainer);
        } else {
            log.info("Container is not reusable, key=[{}] ", key);
            return getStartedContainerInternal(startedContainersBySourceAndId, key, requestedContainer);
        }
    }

    private StartedContainerInternal getStartedContainerInternal(ConcurrentMap<String, Value<StartedContainerInternal>> startedContainersByKey,
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
                    result = knownDockerContainers.prepareForStart(requestedContainer);
                    result.start();
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
    private static class Value<T extends StartedContainerInternal> {

        @NonFinal
        volatile T ref;
        Lock lock = new ReentrantLock();
    }
}
