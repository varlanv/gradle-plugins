package io.huskit.containers.api.ps.arg;

import io.huskit.containers.api.HtArg;

import java.util.stream.Stream;

public interface HtPsArgs {

    static HtPsArgs empty() {
        return HtNoPsArgs.INSTANCE;
    }

    boolean isEmpty();

    int size();

    Stream<HtArg> stream();
}
