package io.huskit.containers.integration;

import io.huskit.common.HtConstants;
import io.huskit.containers.api.image.HtImgName;
import io.huskit.containers.integration.mongo.ContainerHash;
import io.huskit.containers.model.ContainerType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class DefContainerSpec implements ContainerSpec {

    HtImgName image;
    ContainerType containerType;
    DefEnvSpec envSpec = new DefEnvSpec(this);
    DefLabelSpec labelSpec = new DefLabelSpec(this);
    DefWaitSpec waitSpec = new DefWaitSpec(this);
    DefReuseSpec reuseSpec = new DefReuseSpec(this);
    DefPortSpec portSpec = new DefPortSpec(this);
    Map<String, String> properties = new LinkedHashMap<>();

    @Override
    public EnvSpec env() {
        return envSpec;
    }

    @Override
    public LabelSpec labels() {
        return labelSpec;
    }

    @Override
    public WaitSpec await() {
        return waitSpec;
    }

    @Override
    public ReuseSpec reuse() {
        return reuseSpec;
    }

    @Override
    public DefPortSpec ports() {
        return portSpec;
    }

    @Override
    public String hash() {
        return new ContainerHash()
            .add(image.reference())
            .add(envSpec().envMap())
            .add(labelSpec().labelMap().require().entrySet().stream()
                .filter(it ->
                    !HtConstants.CONTAINER_CLEANUP_AFTER_LABEL.equals(it.getKey())
                        && !HtConstants.CONTAINER_STARTED_AT_LABEL.equals(it.getKey())
                        && !HtConstants.CONTAINER_HASH_LABEL.equals(it.getKey())
                )
                .collect(Collectors.toList()))
            .add(waitSpec().textWait())
            .add(reuseSpec().value().check(ReuseWithTimeout::enabled))
//                .add(properties)
//                .add(portSpec.port())
            .compute();
    }

    public DefContainerSpec addProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public Optional<String> prop(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    public Boolean booleanProp(String key) {
        return Boolean.parseBoolean(properties.get(key));
    }
}
