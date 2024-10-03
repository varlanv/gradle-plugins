package io.huskit.containers.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefContainerSpec implements ContainerSpec {

    DefEnvSpec envSpec = new DefEnvSpec(this);
    DefLabelSpec labelSpec = new DefLabelSpec(this);
    DefWaitSpec waitSpec = new DefWaitSpec(this);
    DefReuseSpec reuseSpec = new DefReuseSpec(this);

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
}
