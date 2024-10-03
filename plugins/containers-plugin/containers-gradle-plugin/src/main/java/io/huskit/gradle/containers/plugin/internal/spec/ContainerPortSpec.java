package io.huskit.gradle.containers.plugin.internal.spec;

import io.huskit.common.port.ContainerPort;
import io.huskit.common.port.DynamicContainerPort;
import io.huskit.common.port.FixedContainerPort;
import io.huskit.common.port.FixedRangePort;
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
            //todo format example
            throw new IllegalArgumentException("Fixed port must be set to either `hostValue` or `hostRange`."
                    + " For example, `port { fixed { hostValue(8080) } } or `port { fixed { hostRange(8080, 8081) } }`");
        }
        var containerValue = fixedContainerPortSpec.getContainerValue();
        if (!containerValue.isPresent()) {
            Optional.ofNullable(getContainerDefaultPort().getOrNull())
                    .ifPresentOrElse(containerValue::set, () -> {
                        // todo add example
                        throw new IllegalArgumentException("Container port must be set to `containerValue`");
                    });
        }
        getDynamic().set(false);
    }

    default ContainerPort resolve(ContainerPortSpec portSpec) {
        return Optional.ofNullable(portSpec.getFixed().getOrNull())
                .flatMap(fixedPort -> getFixedPort(fixedPort).or(() -> getFixedRangePort(fixedPort)))
                .orElseGet(DynamicContainerPort::new);
    }

    private Optional<ContainerPort> getFixedPort(FixedContainerPortSpec fixedPort) {
        return Optional.ofNullable(fixedPort.getHostValue().getOrNull())
                .map(hostValue -> new FixedContainerPort(hostValue, fixedPort.getContainerValue().get()));
    }

    private Optional<ContainerPort> getFixedRangePort(FixedContainerPortSpec fixedPort) {
        return Optional.ofNullable(fixedPort.getHostRange().getOrNull())
                .map(hostRange -> new FixedRangePort(hostRange.left(), hostRange.right(), fixedPort.getContainerValue().get()));
    }
}
