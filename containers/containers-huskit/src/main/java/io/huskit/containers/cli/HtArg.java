package io.huskit.containers.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

interface HtArg {

    String name();

    List<String> values();

    default String singleValue() {
        var values = values();
        if (values.isEmpty()) {
            throw new NoSuchElementException();
        } else if (values.size() > 1) {
            throw new IllegalStateException(String.format("Could not return single value for list with multiple values: %s", values));
        } else {
            return values.get(0);
        }
    }

    static HtArg of(CharSequence name, CharSequence value) {
        return new HtDefaultArg(name, Collections.singletonList(value.toString()));
    }

    static HtArg of(CharSequence name, String... values) {
        return new HtDefaultArg(name, Arrays.asList(values));
    }

    static HtArg of(CharSequence name, List<String> values) {
        return new HtDefaultArg(name, values);
    }

    static HtArg of(CharSequence name) {
        return new HtDefaultArg(name.toString(), List.of());
    }
}
