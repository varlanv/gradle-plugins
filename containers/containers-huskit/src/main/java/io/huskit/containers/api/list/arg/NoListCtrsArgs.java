package io.huskit.containers.api.list.arg;

import io.huskit.containers.api.HtArg;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NoListCtrsArgs implements HtListContainersArgs {

    static final NoListCtrsArgs INSTANCE = new NoListCtrsArgs();

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Stream<HtArg> stream() {
        return Stream.empty();
    }
}
