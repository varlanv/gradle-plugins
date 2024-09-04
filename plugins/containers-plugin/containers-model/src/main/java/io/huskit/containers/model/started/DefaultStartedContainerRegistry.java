package io.huskit.containers.model.started;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultStartedContainerRegistry implements StartedContainerRegistry {

    ConcurrentMap<String, Value<? extends StartedContainerInternal>> startedContainersById = new ConcurrentHashMap<>();

    @Override
    public <T extends StartedContainerInternal> T startAndRegister(String id, T startedContainerInternal, Runnable start) {
        var value = startedContainersById.computeIfAbsent(id, key -> new Value<>(startedContainerInternal));
        boolean isLocked = false;
        try {
            if (value.isStarted) {
                return (T) value.ref;
            }
            value.lock.lock();
            isLocked = true;
            if (!value.isStarted) {
                start.run();
                value.isStarted = true;
            }
            return (T) value.ref;
        } finally {
            if (isLocked) {
                value.lock.unlock();
            }
        }
    }

    @RequiredArgsConstructor
    public static class Value<T extends StartedContainerInternal> {

        T ref;
        @NonFinal
        boolean isStarted = false;
        Lock lock = new ReentrantLock();
    }
}
