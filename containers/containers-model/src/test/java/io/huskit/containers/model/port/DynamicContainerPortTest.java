package io.huskit.containers.model.port;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicContainerPortTest implements UnitTest {

    @Test
    @DisplayName("`hostValue` should allocate random port")
    void hostValue_should_allocate_random_port() {
        var port = new DynamicContainerPort();

        var portNumber = port.hostValue();

        assertThat(portNumber).isGreaterThan(0);
    }

    @Test
    @DisplayName("`hostValue` should allocate different ports")
    void hostValue_should_allocate_different_ports() {
        var port1 = new DynamicContainerPort();
        var port2 = new DynamicContainerPort();

        var portNumber1 = port1.hostValue();
        var portNumber2 = port2.hostValue();

        assertThat(portNumber1).isNotEqualTo(portNumber2);
    }

    @Test
    @DisplayName("`containerValue` should be empty")
    void containerValue_should_be_empty() {
        var port = new DynamicContainerPort();

        assertThat(port.containerValue()).isEmpty();
    }

    @Test
    @DisplayName("`isFixed` should return false")
    void isFixed_should_return_false() {
        var port = new DynamicContainerPort();

        assertThat(port.isFixed()).isFalse();
    }
}
