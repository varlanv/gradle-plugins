package io.huskit.containers.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@RequiredArgsConstructor
public class DfHtStartedContainer implements HtStartedContainer {

    String id;
    String hash;
    Map<String, String> props;
    AtomicBoolean stopped = new AtomicBoolean(false);

    @Override
    @UnmodifiableView
    public Map<String, String> properties() {
        return Collections.unmodifiableMap(props);
    }

    @Override
    public void stopAndRemove() {
        stopped.set(true);
    }

    @Override
    public Boolean isStopped() {
        return stopped.get();
    }
}
