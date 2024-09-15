package io.huskit.containers.integration.mongo;

import io.huskit.common.Volatile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class DefWaitSpec implements WaitSpec {

    ContainerSpec parent;
    Volatile<Waiter> logMessage = Volatile.of();

    @Override
    public ContainerSpec forLogMessageContaining(CharSequence text, Duration timeout) {
        this.logMessage.set(new Waiter(text.toString(), timeout));
        return parent;
    }
}
