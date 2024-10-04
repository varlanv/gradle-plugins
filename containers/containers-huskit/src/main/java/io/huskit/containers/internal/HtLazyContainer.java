package io.huskit.containers.internal;

import io.huskit.containers.api.HtContainer;
import io.huskit.containers.api.HtContainerConfig;
import io.huskit.containers.api.HtContainerNetwork;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class HtLazyContainer implements HtContainer {

    @Getter
    String id;
    Supplier<HtContainer> delegate;

    @Override
    public String name() {
        return delegate.get().name();
    }

    @Override
    public HtContainerConfig config() {
        return delegate.get().config();
    }

    @Override
    public HtContainerNetwork network() {
        return delegate.get().network();
    }

    @Override
    public Instant createdAt() {
        return delegate.get().createdAt();
    }

    @Override
    public String toString() {
        return delegate.get().toString();
    }
}
