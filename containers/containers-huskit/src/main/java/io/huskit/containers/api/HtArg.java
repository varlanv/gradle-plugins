package io.huskit.containers.api;

import io.huskit.containers.HtDefaultArg;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public interface HtArg {

    String name();

    List<String> values();

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
