package io.huskit.containers.internal;

import io.huskit.containers.api.HtContainer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Map;
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
    public Map<String, String> labels() {
        return delegate.get().labels();
    }

    @Override
    public Instant createdAt() {
        return delegate.get().createdAt();
    }

    @Override
    public Integer firstMappedPort() {
        return delegate.get().firstMappedPort();
    }
}
