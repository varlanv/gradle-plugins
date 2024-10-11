package io.huskit.containers.cli;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class HtDefaultArg implements HtArg {

    String name;
    List<String> values;

    public HtDefaultArg(CharSequence name, List<String> values) {
        this(name.toString(), values);
    }
}
