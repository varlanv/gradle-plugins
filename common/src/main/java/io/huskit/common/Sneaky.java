package io.huskit.common;

import io.huskit.common.function.ThrowingRunnable;

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
}
