package io.huskit.common;

import io.huskit.common.function.ThrowingRunnable;
import io.huskit.common.function.ThrowingSupplier;
import lombok.SneakyThrows;

import java.util.ArrayList;

public interface Sneaky {

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T rethrow(Throwable t) throws T {
        throw (T) t;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    static Runnable quiet(ThrowingRunnable... actions) {
        return () -> {
            for (var action : actions) {
                try {
                    action.run();
                } catch (Exception ignore) {
                    // no-op
                }
            }
        };
    }

    @SneakyThrows
    static <T> T quiet(ThrowingSupplier<T> supplier) {
        return supplier.get();
    }

    static <T> ThrowingSupplier<T> thrown(ThrowingSupplier<? extends Throwable> e) {
        return () -> hide(e);
    }

    static void tryQuietly(ThrowingRunnable... runnables) {
        for (var runnable : runnables) {
            try {
                runnable.run();
            } catch (Exception ignore) {
                // no-op
            }
        }
    }

    static void tryAll(ThrowingRunnable... runnables) {
        var exceptions = new ArrayList<Exception>(runnables.length);
        for (var runnable : runnables) {
            try {
                runnable.run();
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        if (!exceptions.isEmpty()) {
            var runtimeException = new RuntimeException();
            exceptions.forEach(runtimeException::addSuppressed);
            throw runtimeException;
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T hideType(Object anyType) {
        return (T) anyType;
    }

    @SneakyThrows
    private static <T> T hide(ThrowingSupplier<? extends Throwable> supplier) {
        throw supplier.get();
    }
}
