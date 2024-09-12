package io.huskit.containers.api.ps.arg;

import io.huskit.containers.api.HtArg;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class HtListPsArgs implements HtPsArgs {

    List<HtArg> list;

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Stream<HtArg> stream() {
        return list.stream();
    }
}
