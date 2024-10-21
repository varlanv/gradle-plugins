package io.huskit.common;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public class FakeTestLog implements Log {

    Queue<String> debugMessages = new ConcurrentLinkedQueue<>();

    @Override
    public void debug(Supplier<String> message) {
        debugMessages.add(message.get());
    }

    public List<String> debugMessages() {
        return List.copyOf(debugMessages);
    }
}
