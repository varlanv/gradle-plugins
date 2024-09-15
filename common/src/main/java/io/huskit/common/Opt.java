
package io.huskit.common;

import java.util.function.Consumer;

public interface Opt<T> {

    static <T> Opt<T> of(T value) {
        return new Some<>(value);
    }

    static <T> Opt<T> empty() {
        return None.instance();
    }

    T require(String missingValueMessage);

    default T require() {
        return require("No value present");
    }

    boolean isPresent();

    boolean isEmpty();

    void ifPresent(Consumer<T> consumer);
}
