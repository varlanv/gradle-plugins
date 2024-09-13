package io.huskit.containers.api.run;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

@RequiredArgsConstructor
public class HtCliRunOptions implements HtRunOptions {

    Map<HtOptionType, HtOption> optionMap;
    int size;

    public HtCliRunOptions() {
        this(new EnumMap<>(HtOptionType.class), 0);
    }

    @Override
    public HtRunOptions withLabels(Map<String, String> labels) {
        var newOptionMap = new EnumMap<>(optionMap);
        newOptionMap.put(HtOptionType.LABELS, new MapHtOption(HtOptionType.LABELS, Map.copyOf(labels)));
        return new HtCliRunOptions(newOptionMap, size + labels.size());
    }

    @Override
    public HtRunOptions withCommand(CharSequence command) {
        var newOptionMap = new EnumMap<>(optionMap);
        newOptionMap.put(HtOptionType.COMMAND, new CmdHtOpt(command));
        return new HtCliRunOptions(newOptionMap, size + 1);
    }

    @Override
    public Map<HtOptionType, HtOption> asMap() {
        return Collections.unmodifiableMap(optionMap);
    }

    @Override
    public int size() {
        return size;
    }
}
