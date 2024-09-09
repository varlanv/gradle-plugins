package io.huskit.containers.model.port;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.BindException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedContainerPortTest implements UnitTest {

    Integer hostPort = 11119;
    Integer containerPort = 2;

    @Test
    @DisplayName("`containerValue` should return container port")
    void test_0() {
        assertThat(new FixedContainerPort(hostPort, containerPort).containerValue()).contains(containerPort);
    }

    @Test
    @DisplayName("when trying to pass null container port, should throw exception")
    void test_1() {
        assertThatThrownBy(() -> new FixedContainerPort(hostPort, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("containerValue is marked non-null but is null");
    }

    @Test
    @DisplayName("`isFixed` should return true")
    void test_2() {
        assertThat(new FixedContainerPort(hostPort, containerPort).isFixed()).isTrue();
    }

    @Test
    @DisplayName("`hostValue` should return value or throw bind exception if port is already in use")
    void test_3() {
        // this test check two cases at once to avoid flakiness
        var exceptionRef = new AtomicReference<Throwable>();
        parallel(5, () -> {
            try {
                assertThat(new FixedContainerPort(hostPort, containerPort).hostValue()).isEqualTo(hostPort);
            } catch (Throwable e) {
                exceptionRef.set(e);
            }
        });
        if (exceptionRef.get() != null) {
            assertThat(exceptionRef.get().getCause())
                    .isInstanceOf(BindException.class);
        }
    }
}
