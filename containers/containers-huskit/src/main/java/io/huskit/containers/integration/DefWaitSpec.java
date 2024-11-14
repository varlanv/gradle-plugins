package io.huskit.containers.integration;

import io.huskit.common.Mutable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class DefWaitSpec implements WaitSpec {

    DefContainerSpec parent;
    Mutable<TextWait> textWait = Mutable.of();

    @Override
    public DefContainerSpec forLogMessageContaining(CharSequence text, Duration timeout) {
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout must be positive (or zero for no timeout). Received: " + timeout);
        }
        this.textWait.set(new TextWait(text.toString(), timeout));
        return parent;
    }

    @Override
    public DefContainerSpec forLogMessageContaining(CharSequence text) {
        return this.forLogMessageContaining(text, Duration.ofMinutes(2));
    }
}
