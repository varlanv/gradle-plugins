package io.huskit.containers.model.started;

import io.huskit.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class ContainerLauncher {

    private static final int TIMEOUT = 10;
    Log log;
    List<Supplier<StartedContainerInternal>> containers;

    public ContainerLauncher(Log log, StartedContainerInternal... containers) {
        this(log, Arrays.stream(containers)
                .map(container -> (Supplier<StartedContainerInternal>) () -> container)
                .collect(Collectors.toList()));
    }

    public List<StartedContainer> start() {
        return doParallel(StartedContainerInternal::start);
    }

    public List<StartedContainer> stop() {
        return doParallel(this::tryClose);
    }

    @SneakyThrows
    public List<StartedContainer> doParallel(Consumer<StartedContainerInternal> startedContainersInternalConsumer) {
        var size = containers.size();
        if (size == 1) {
            var startedContainerInternal = containers.get(0).get();
            startedContainerInternal.start();
            return List.of(startedContainerInternal);
        } else if (size > 1) {
            var executor = Executors.newFixedThreadPool(size);
            var latch = new CountDownLatch(size);
            var containers = new StartedContainerInternal[size];
            try {
                for (var i = 0; i < size; i++) {
                    var containerSupplier = this.containers.get(i);
                    final var idx = i;
                    executor.submit(() -> {
                        var startedContainerInternal = containerSupplier.get();
                        startedContainersInternalConsumer.accept(startedContainerInternal);
                        latch.countDown();
                        containers[idx] = startedContainerInternal;
                    });
                }
                latch.await(TIMEOUT, TimeUnit.SECONDS);
                return Arrays.asList(containers);
            } finally {
                executor.shutdownNow();
            }
        } else {
            return List.of();
        }
    }

    private void tryClose(StartedContainerInternal container) {
        try {
            container.close();
        } catch (Exception e) {
            log.error("Failed to stop container [{}]. Ignoring exception", container.id(), e);
        }
    }
}
