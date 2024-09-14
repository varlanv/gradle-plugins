package io.huskit.containers.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalCliRecorder implements CliRecorder {

    Map<Long, List<HtCommand>> commands = new ConcurrentHashMap<>();

    @Override
    public void record(HtCommand command) {
        commands.computeIfAbsent(Thread.currentThread().getId(), k -> new ArrayList<>()).add(command);
    }

    public List<HtCommand> forCurrentThread() {
        return Objects.requireNonNullElseGet(commands.get(Thread.currentThread().getId()), ArrayList::new);
    }

    public void clearForCurrentThread() {
        commands.remove(Thread.currentThread().getId());
    }
}
