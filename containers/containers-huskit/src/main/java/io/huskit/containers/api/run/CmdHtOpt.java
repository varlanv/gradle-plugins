package io.huskit.containers.api.run;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class CmdHtOpt implements HtOption {

    CharSequence command;

    @Override
    public HtOptionType type() {
        return HtOptionType.COMMAND;
    }

    @Override
    public Map<String, String> map() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String singleValue() {
        return command.toString();
    }
}
