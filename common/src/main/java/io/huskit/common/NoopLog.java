package io.huskit.common;

import java.util.function.Supplier;

public class NoopLog implements Log {

    @Override
    public void debug(Supplier<String> message) {
        // no-op
    }
}
