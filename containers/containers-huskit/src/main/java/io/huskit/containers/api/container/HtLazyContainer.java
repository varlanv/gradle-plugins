package io.huskit.containers.api.container;

import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.function.ThrowingSupplier;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class HtLazyContainer implements HtContainer {

    @Getter
    String id;
    MemoizedSupplier<HtContainer> delegate;

    public HtLazyContainer(String id, ThrowingSupplier<HtContainer> delegate) {
        this.id = id;
        this.delegate = MemoizedSupplier.of(delegate);
    }

    @Override
    public String name() {
        return delegate.get().name();
    }

    @Override
    public HtContainerConfig config() {
        return delegate.get().config();
    }

    @Override
    public HtContainerNetworkSettings network() {
        return delegate.get().network();
    }

    @Override
    public Instant createdAt() {
        return delegate.get().createdAt();
    }

    @Override
    public List<String> args() {
        return delegate.get().args();
    }

    @Override
    public String path() {
        return delegate.get().path();
    }

    @Override
    public String processLabel() {
        return delegate.get().processLabel();
    }

    @Override
    public String platform() {
        return delegate.get().platform();
    }

    @Override
    public String driver() {
        return delegate.get().driver();
    }

    @Override
    public HtContainerGraphDriver graphDriver() {
        return delegate.get().graphDriver();
    }

    @Override
    public String hostsPath() {
        return delegate.get().hostsPath();
    }

    @Override
    public String hostnamePath() {
        return delegate.get().hostnamePath();
    }

    @Override
    public Integer restartCount() {
        return delegate.get().restartCount();
    }

    @Override
    public String mountLabel() {
        return delegate.get().mountLabel();
    }

    @Override
    public HtContainerState state() {
        return delegate.get().state();
    }

    @Override
    public HtContainerHostConfig hostConfig() {
        return delegate.get().hostConfig();
    }

    @Override
    public String resolvConfPath() {
        return delegate.get().resolvConfPath();
    }

    @Override
    public String logPath() {
        return delegate.get().logPath();
    }

    @Override
    public Map<String, Object> toJsonMap() {
        return delegate.get().toJsonMap();
    }

    @Override
    public String toString() {
        return delegate.get().toString();
    }
}
