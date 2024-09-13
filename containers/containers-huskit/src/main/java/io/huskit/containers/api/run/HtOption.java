package io.huskit.containers.api.run;

import java.util.Map;

public interface HtOption {

    HtOptionType type();

    Map<String, String> map();

    String singleValue();
}
