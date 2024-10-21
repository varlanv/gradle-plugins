package io.huskit.common;

import java.util.function.Supplier;

public interface Log {

    void debug(Supplier<String> message);
}
