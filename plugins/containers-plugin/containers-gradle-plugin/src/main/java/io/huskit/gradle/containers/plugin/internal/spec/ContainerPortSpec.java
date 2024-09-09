package io.huskit.gradle.containers.plugin.internal.spec;

import io.huskit.containers.model.port.ContainerPort;
import io.huskit.containers.model.port.DynamicContainerPort;
import io.huskit.containers.model.port.FixedContainerPort;
import io.huskit.containers.model.port.FixedRangePort;
import io.huskit.gradle.containers.plugin.api.ContainerPortSpecView;
import io.huskit.gradle.containers.plugin.api.FixedContainerPortSpecView;
import org.gradle.api.Action;
import org.gradle.api.provider.Property;

import java.util.Optional;

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

    default ContainerPort resolve(ContainerPortSpec portSpec) {
        return Optional.ofNullable(portSpec.getFixed().getOrNull())
                .map(fixedPort -> {
                    var hostValue = fixedPort.getHostValue().getOrNull();
                    var hostRange = fixedPort.getHostRange().getOrNull();
                    if (hostValue != null) {
                        return new FixedContainerPort(hostValue, fixedPort.getContainerValue().get());
                    } else if (hostRange != null) {
                        return new FixedRangePort(hostRange.left(), hostRange.right(), fixedPort.getContainerValue().get());
                    } else {
                        return null;
                    }
                })
                .orElseGet(DynamicContainerPort::new);
    }
}
