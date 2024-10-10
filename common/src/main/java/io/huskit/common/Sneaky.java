package io.huskit.common;

import io.huskit.common.function.ThrowingRunnable;
import io.huskit.common.function.ThrowingSupplier;
import lombok.SneakyThrows;

public interface Sneaky {

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T rethrow(Throwable t) throws T {
        throw (T) t;
    }

    @SuppressWarnings("PMD.EmptyCatchBlock")
    static Runnable quiet(ThrowingRunnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception ignore) {
                // no-op
            }
        };
    }

    static <T> ThrowingSupplier<T> thrown(ThrowingSupplier<? extends Throwable> e) {
        return () -> hide(e);
    }

    @SneakyThrows
    private static <T> T hide(ThrowingSupplier<? extends Throwable> supplier) {
        throw supplier.get();
    }
}
