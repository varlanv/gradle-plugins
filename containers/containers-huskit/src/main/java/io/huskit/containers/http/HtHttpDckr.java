package io.huskit.containers.http;

import io.huskit.common.Log;
import io.huskit.containers.api.container.HtContainers;
import io.huskit.containers.api.image.HtImages;
import io.huskit.containers.api.volume.HtVolumes;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.time.Duration;
import java.util.Objects;

@RequiredArgsConstructor
public final class HtHttpDckr implements HtHttpDocker {

    DfHtHttpDockerSpec spec;

    public HtHttpDckr() {
        this.spec = new DfHtHttpDockerSpec();
    }

    @Override
    public HtHttpDckr withCleanOnClose(Boolean cleanOnClose) {
        if (!Objects.equals(spec.isCleanOnClose(), cleanOnClose)) {
            return new HtHttpDckr(spec.withIsCleanOnClose(cleanOnClose));
        }
        return this;
    }

    @Override
    public HtHttpDckr withLog(Log log) {
        return new HtHttpDckr(
            spec.withLog(log)
        );
    }

    @Override
    public HtHttpDckr withDefaultTimeout(Duration timeout) {
        return new HtHttpDckr(
            spec.withDefaultTimeout(timeout)
        );
    }

    @Override
    public HtContainers containers() {
        return new HtHttpContainers(spec, images());
    }

    @Override
    public HtImages images() {
        return new HtHttpImages(spec);
    }

    @Override
    public HtVolumes volumes() {
        return null;
    }

    @Override
    @SneakyThrows
    public void close() {
        spec.socket().release();
    }
}
