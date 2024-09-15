package io.huskit.gradle.containers.plugin.internal.spec;

import io.huskit.common.Tuple;
import io.huskit.gradle.containers.plugin.api.FixedContainerPortSpecView;
import org.gradle.api.provider.Property;

import java.util.List;

public interface FixedContainerPortSpec extends FixedContainerPortSpecView {

    Property<Integer> getHostValue();

    Property<Tuple<Integer, Integer>> getHostRange();

    Property<Integer> getContainerValue();

    @Override
    default void hostValue(Integer hostPort) {
        if (getHostRange().isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Can't set port to `hostValue` [%s] because it is already set to `hostRange` %s",
                            hostPort, getHostRange().get().toList()
                    ));
        } else if (hostPort <= 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Fixed port [%s] must be greater than 0",
                            hostPort
                    ));
        }
        getHostValue().set(hostPort);
    }

    @Override
    default void hostRange(Integer hostPortFrom, Integer hostPortTo) {
        if (getHostValue().isPresent()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Can't set port to `hostRange` %s because it is already set to `hostValue` [%s]",
                            List.of(hostPortFrom, hostPortTo), getHostValue().get()
                    ));
        } else if (hostPortFrom <= 0 || hostPortTo <= 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Fixed port range %s must be greater than 0",
                            List.of(hostPortFrom, hostPortTo)
                    ));
        } else if (hostPortFrom >= hostPortTo) {
            throw new IllegalArgumentException(
                    String.format(
                            "Range start [%s] for `hostRange` must be less than range end [%s]",
                            hostPortFrom, hostPortTo
                    ));
        }
        getHostRange().set(Tuple.of(hostPortFrom, hostPortTo));
    }

    @Override
    default void containerValue(Integer value) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    String.format(
                            "Container port [%s] must be greater than 0",
                            value
                    ));
        }
        getContainerValue().set(value);
    }
}
