package io.huskit.common;

import io.huskit.common.function.MemoizedSupplier;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

public interface Log {

    void info(Supplier<String> message);

    void debug(Supplier<String> message);

    void error(Supplier<String> message);

    static Log std() {
        return new StdLog();
    }

    static Log noop() {
        return NoopLog.INSTANCE;
    }

    static Log conditional(Log delegate, MemoizedSupplier<Boolean> condition) {
        return new ConditionalLog(delegate, condition);
    }

    static Log.Fake fake() {
        return new FakeLog();
    }

    static Log.Fake fakeVerbose() {
        return new FakeVerboseLog();
    }

    interface Fake extends Log {

        List<String> debugMessages();

        List<String> infoMessages();

        List<String> errorMessages();
    }
}

final class FakeLog implements Log.Fake {

    Queue<String> debugMessages = new ConcurrentLinkedQueue<>();
    Queue<String> infoMessages = new ConcurrentLinkedQueue<>();
    Queue<String> errorMessages = new ConcurrentLinkedQueue<>();

    @Override
    public void info(Supplier<String> message) {
        var msg = message.get();
        infoMessages.add(msg);
    }

    @Override
    public void debug(Supplier<String> message) {
        var msg = message.get();
        debugMessages.add(msg);
    }

    @Override
    public void error(Supplier<String> message) {
        var msg = message.get();
        errorMessages.add(msg);
    }

    @Override
    public List<String> debugMessages() {
        return List.copyOf(debugMessages);
    }

    @Override
    public List<String> infoMessages() {
        return List.copyOf(infoMessages);
    }

    @Override
    public List<String> errorMessages() {
        return List.copyOf(errorMessages);
    }
}

final class FakeVerboseLog implements Log.Fake {

    Queue<String> debugMessages = new ConcurrentLinkedQueue<>();
    Queue<String> infoMessages = new ConcurrentLinkedQueue<>();
    Queue<String> errorMessages = new ConcurrentLinkedQueue<>();

    @Override
    public void info(Supplier<String> message) {
        var msg = message.get();
        System.out.println("INFO: " + msg);
        infoMessages.add(msg);
    }

    @Override
    public void debug(Supplier<String> message) {
        var msg = message.get();
        System.out.println("DEBUG: " + msg);
        debugMessages.add(msg);
    }

    @Override
    public void error(Supplier<String> message) {
        var msg = message.get();
        System.err.println("ERROR: " + msg);
        errorMessages.add(msg);
    }

    @Override
    public List<String> debugMessages() {
        return List.copyOf(debugMessages);
    }

    @Override
    public List<String> errorMessages() {
        return List.copyOf(errorMessages);
    }

    @Override
    public List<String> infoMessages() {
        return List.copyOf(infoMessages);
    }
}

final class NoopLog implements Log {

    static final Log INSTANCE = new NoopLog();

    @Override
    public void info(Supplier<String> message) {
        // no-op
    }

    @Override
    public void debug(Supplier<String> message) {
        // no-op
    }

    @Override
    public void error(Supplier<String> message) {
        // no-op
    }
}

final class StdLog implements Log {

    @Override
    public void info(Supplier<String> message) {
        System.out.println(message.get());
    }

    @Override
    public void debug(Supplier<String> message) {
        System.out.println(message.get());
    }

    @Override
    public void error(Supplier<String> message) {
        System.err.println(message.get());
    }
}

@RequiredArgsConstructor
class ConditionalLog implements Log {

    Log delegate;
    MemoizedSupplier<Boolean> condition;

    @Override
    public void info(Supplier<String> message) {
        if (condition.get()) {
            delegate.info(message);
        }
    }

    @Override
    public void debug(Supplier<String> message) {
        if (condition.get()) {
            delegate.debug(message);
        }
    }

    @Override
    public void error(Supplier<String> message) {
        if (condition.get()) {
            delegate.error(message);
        }
    }
}
