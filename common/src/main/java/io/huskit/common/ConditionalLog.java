package io.huskit.common;

import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ConditionalLog implements Log {

    Log delegate;
    MemoizedSupplier<Boolean> condition;

    @Override
    public void debug(Supplier<String> message) {
        if (condition.get()) {
            delegate.debug(message);
        }
    }
}
