package io.huskit.gradle.containers.plugin.api;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;

public interface ContainerPortSpec extends ContainerPortSpecView {

    Property<Boolean> getDynamic();

    Property<FixedContainerPortSpec> getFixed();

    Property<Integer> getContainerDefaultPort();

    @Override
    default void fixed(Action<FixedContainerPortSpecView> action) {
        var fixedContainerPortSpec = getFixed().get();
        action.execute(fixedContainerPortSpec);
        if (!fixedContainerPortSpec.getHostValue().isPresent() && !fixedContainerPortSpec.getHostRange().isPresent()) {
            throw new IllegalArgumentException("Fixed port must be set to either `hostValue` or `hostRange`. For example, `port { fixed { hostValue(8080) } } or `port { fixed { hostRange(8080, 8081) } }`"); //todo format example
        }
        var containerValue = fixedContainerPortSpec.getContainerValue();
        var containerValueProp = containerValue.getOrNull();
        if (containerValueProp == null) {
            var containerDefaultPort = getContainerDefaultPort().getOrNull();
            if (containerDefaultPort == null) {
                throw new IllegalArgumentException("Container port must be set to `containerValue`");// todo add example
            } else {
                containerValue.set(containerDefaultPort);
            }
        }
        getDynamic().set(false);
    }
}
