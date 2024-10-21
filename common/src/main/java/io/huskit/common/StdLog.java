package io.huskit.common;

import java.util.function.Supplier;

public class StdLog implements Log {

    @Override
    public void debug(Supplier<String> message) {
        System.out.println(message.get());
    }
}
