package io.huskit.containers.api.run;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class MapHtOption implements HtOption {

    HtOptionType type;
    Map<String, String> map;
}
