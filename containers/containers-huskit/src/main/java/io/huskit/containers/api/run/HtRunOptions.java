package io.huskit.containers.api.run;

import java.util.Map;

public interface HtRunOptions {

    HtRunOptions withLabels(Map<String, String> labels);

    Map<HtOptionType, HtOption> asMap();

    int size();
}
