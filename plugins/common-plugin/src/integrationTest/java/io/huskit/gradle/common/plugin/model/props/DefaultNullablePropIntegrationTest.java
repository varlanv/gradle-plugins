package io.huskit.gradle.common.plugin.model.props;

import io.huskit.gradle.commontest.BaseGradleIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultNullablePropIntegrationTest extends BaseGradleIntegrationTest {

    String propName = "anyPropName";
    String propVal = "anyPropVal";

    @Test
    @DisplayName("'name' should return prop name")
    void test_0() {
        var project = setupProject();
        var subject = new DefaultNullableProp(propName, project.getProviders().provider(() -> propVal));

        assertThat(subject.name()).isEqualTo(propName);
    }

    @Test
    @DisplayName("'value' should return prop value")
    void test_1() {
        var project = setupProject();
        var subject = new DefaultNullableProp(propName, project.getProviders().provider(() -> propVal));

        assertThat(subject.value()).isEqualTo(propVal);
    }

    @Test
    @DisplayName("'stringValue' should return prop value")
    void test_2() {
        var project = setupProject();
        var subject = new DefaultNullableProp(propName, project.getProviders().provider(() -> propVal));

        assertThat(subject.stringValue()).isEqualTo(propVal);
    }

    @MethodSource
    @ParameterizedTest
    @DisplayName("'holdsTrue' should return true if prop value is true")
    void test_3(String truth, boolean expected) {
        var project = setupProject();
        var subject = new DefaultNullableProp(propName, project.getProviders().provider(() -> truth));

        assertThat(subject.holdsTrue()).isEqualTo(expected);
    }

    static Stream<Arguments> test_3() {
        return Stream.of(
                Arguments.of("tRuE", true),
                Arguments.of("true", true),
                Arguments.of("TRUE", true),
                Arguments.of("false", false),
                Arguments.of(null, false),
                Arguments.of("", false)
        );
    }

    @MethodSource
    @ParameterizedTest
    @DisplayName("'holdsFalse' should return true if prop value is false")
    void test_4(String truth, boolean expected) {
        var project = setupProject();
        var subject = new DefaultNullableProp(propName, project.getProviders().provider(() -> truth));

        assertThat(subject.holdsFalse()).isEqualTo(expected);
    }

    static Stream<Arguments> test_4() {
        return Stream.of(
                Arguments.of("false", true),
                Arguments.of("FALSE", true),
                Arguments.of("FaLsE", true),
                Arguments.of("true", false),
                Arguments.of(null, false),
                Arguments.of("", false)
        );
    }
}
