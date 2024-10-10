package io.huskit.containers.integration;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class FakeHtIntegratedDocker implements HtIntegratedDocker {

    Map<String, DefContainerSpec> specs = new ConcurrentHashMap<>();
    AtomicBoolean stopped = new AtomicBoolean(false);
    Map<String, HtStartedContainer> containers;

    public FakeHtIntegratedDocker(Map<String, HtStartedContainer> containers) {
        this.containers = new ConcurrentHashMap<>(containers);
    }

    public FakeHtIntegratedDocker(HtStartedContainer container) {
        this.containers = new ConcurrentHashMap<>(Map.of(container.id(), container));
    }

    public FakeHtIntegratedDocker() {
        this.containers = new ConcurrentHashMap<>();
    }


    @Override
    @UnmodifiableView
    public Map<String, HtStartedContainer> feed(ServicesSpec servicesSpec) {
        servicesSpec.containers().forEach(spec -> specs.put(spec.hash(), (DefContainerSpec) spec));
        return Collections.unmodifiableMap(containers);
    }

    @Override
    public void stop() {
        stopped.set(true);
    }

    public boolean isStopped() {
        return stopped.get();
    }

    public void addContainer(HtStartedContainer container) {
        containers.put(container.id(), container);
    }

    @UnmodifiableView
    public Map<String, DefContainerSpec> receivedSpecs() {
        return Collections.unmodifiableMap(specs);
    }

    public DefContainerSpec receivedSpec() {
        var values = specs.values();
        if (values.isEmpty()) {
            throw new IllegalStateException("No spec received");
        } else if (values.size() > 1) {
            throw new IllegalStateException("More than one spec received");
        } else {
            return values.iterator().next();
        }
    }
}
