package io.huskit.containers.api.list.arg;

import io.huskit.containers.api.HtArg;

import java.util.stream.Stream;

public interface HtListContainersArgs {

    static HtListContainersArgs empty() {
        return NoListCtrsArgs.INSTANCE;
    }

    boolean isEmpty();

    int size();

    Stream<HtArg> stream();
}
