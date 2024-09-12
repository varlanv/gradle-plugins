package io.huskit.containers.api.ps.arg;

import io.huskit.containers.api.HtArg;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HtNoPsArgs implements HtPsArgs {

    static final HtNoPsArgs INSTANCE = new HtNoPsArgs();

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
