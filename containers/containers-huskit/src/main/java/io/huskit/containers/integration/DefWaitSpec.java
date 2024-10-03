package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class DefWaitSpec implements WaitSpec {

    ContainerSpec parent;
    Mutable<TextWait> textWait = Mutable.of();

    @Override
    public ContainerSpec forLogMessageContaining(CharSequence text, Duration timeout) {
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be positive (or zero for no timeout). Received: " + timeout);
        }
        this.textWait.set(new TextWait(text.toString(), timeout));
        return parent;
    }
}
