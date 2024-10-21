package io.huskit.containers.api.container;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class JsonHtContainerHostConfig implements HtContainerHostConfig {

    Map<String, Object> source;
}
