package io.huskit.containers.model.port;

import io.huskit.gradle.commontest.StatelessUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DynamicContainerPortTest implements StatelessUnitTest {

    @Test
    @DisplayName("should allocate random port")
    void test_0() {
        var port = new DynamicContainerPort();

        var portNumber = port.number();

        assertThat(portNumber).isGreaterThan(0);
    }

    @Test
    @DisplayName("should allocate different ports")
    void test_1() {
        var port1 = new DynamicContainerPort();
        var port2 = new DynamicContainerPort();

        var portNumber1 = port1.number();
        var portNumber2 = port2.number();

        assertThat(portNumber1).isNotEqualTo(portNumber2);
    }
}
