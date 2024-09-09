package io.huskit.gradle.containers.plugin.api;

import io.huskit.common.Tuple;
import org.gradle.api.provider.Property;

public interface FixedContainerPortSpec extends FixedContainerPortSpecView {

    Property<Integer> getHostValue();

    Property<Tuple<Integer, Integer>> getHostRange();

    Property<Integer> getContainerValue();

    @Override
    default void hostValue(Integer hostPort) {
        if (getHostRange().isPresent()) {
            throw new IllegalArgumentException("Can't set port to `hostValue` because it is already set to `hostRange`");
        } else if (hostPort <= 0) {
            throw new IllegalArgumentException("Fixed port must be greater than 0");
        }
        getHostValue().set(hostPort);
    }

    @Override
    default void hostRange(Integer hostPortFrom, Integer hostPortTo) {
        if (getHostValue().isPresent()) {
            throw new IllegalArgumentException("Can't set port to `hostRange` because it is already set to `hostValue`");
        } else if (hostPortFrom <= 0 || hostPortTo <= 0) {
            throw new IllegalArgumentException("Fixed port range must be greater than 0");
        } else if (hostPortFrom >= hostPortTo) {
            throw new IllegalArgumentException("Range start for `hostRange` must be less than range end");
        }
        getHostRange().set(new Tuple<>(hostPortFrom, hostPortTo));
    }

    @Override
    default void containerValue(Integer value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Container port must be greater than 0");
        }
        getContainerValue().set(value);
    }
}
