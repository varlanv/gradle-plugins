package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class JsonHtContainerGraphDriver implements HtContainerGraphDriver {

    Map<String, Object> source;

    @Override
    public String name() {
        return HtCollections.getFromMap("Name", source);
    }

    @Override
    public Map<String, Object> data() {
        return HtCollections.getFromMap("Data", source);
    }
}
