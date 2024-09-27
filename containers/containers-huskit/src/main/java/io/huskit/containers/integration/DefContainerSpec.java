package io.huskit.containers.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefContainerSpec implements ContainerSpec {

    DefEnvSpec envSpec = new DefEnvSpec(this);
    DefWaitSpec waitSpec = new DefWaitSpec(this);

    @Override
    public EnvSpec env() {
        return envSpec;
    }

    @Override
    public WaitSpec await() {
        return waitSpec;
    }
}
