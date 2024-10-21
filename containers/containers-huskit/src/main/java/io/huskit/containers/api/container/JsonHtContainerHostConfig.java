package io.huskit.containers.api.container;

import io.huskit.common.collection.HtCollections;
import io.huskit.common.port.MappedPort;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class JsonHtContainerHostConfig implements HtContainerHostConfig {

    Map<String, Object> source;
}
