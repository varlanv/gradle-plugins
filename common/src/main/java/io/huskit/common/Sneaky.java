package io.huskit.common;

public interface Sneaky {

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T rethrow(Throwable t) throws T {
        throw (T) t;
    }
}
