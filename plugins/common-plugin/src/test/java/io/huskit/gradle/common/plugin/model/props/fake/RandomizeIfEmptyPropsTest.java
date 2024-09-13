package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RandomizeIfEmptyPropsTest implements UnitTest {

    String nonExistentPropName = "nonexistent";
    String existingPropName = "existing";
    String existingPropValue = "existingValue";

    @Test
    @DisplayName("if non null property not exists, then should return random value")
    void if_non_null_property_not_exists_then_should_return_random_value() {
        var fakeProps = new FakeProps();
        var subject = new RandomizeIfEmptyProps(fakeProps);

        var actual = subject.nonnull(nonExistentPropName);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo(nonExistentPropName);
        assertThat(actual.value()).isNotNull();
        assertThat(actual.stringValue()).isNotNull();
    }

    @Test
    @DisplayName("if nullable property not exists, then should return random value")
    void if_nullable_property_not_exists_then_should_return_random_value() {
        var fakeProps = new FakeProps();
        var subject = new RandomizeIfEmptyProps(fakeProps);

        var actual = subject.nullable(nonExistentPropName);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo(nonExistentPropName);
        assertThat(actual.value()).isNotNull();
        assertThat(actual.stringValue()).isNotNull();
    }

    @Test
    @DisplayName("if system environment property not exists, then should return random value")
    void if_system_environment_property_not_exists_then_should_return_random_value() {
        var fakeProps = new FakeProps();
        var subject = new RandomizeIfEmptyProps(fakeProps);

        var actual = subject.env(nonExistentPropName);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo(nonExistentPropName);
        assertThat(actual.value()).isNotNull();
        assertThat(actual.stringValue()).isNotNull();
    }

    @Test
    @DisplayName("if non null property exists, then should return it")
    void if_non_null_property_exists_then_should_return_it() {
        var fakeProps = new FakeProps();
        var subject = new RandomizeIfEmptyProps(fakeProps);
        fakeProps.add(existingPropName, existingPropValue);

        var actual = subject.nonnull(existingPropName);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo(existingPropName);
        assertThat(actual.value()).isEqualTo(existingPropValue);
        assertThat(actual.stringValue()).isEqualTo(existingPropValue);
    }

    @Test
    @DisplayName("if nullable property exists, then should return it")
    void if_nullable_property_exists_then_should_return_it() {
        var fakeProps = new FakeProps();
        var subject = new RandomizeIfEmptyProps(fakeProps);
        fakeProps.add(existingPropName, existingPropValue);

        var actual = subject.nullable(existingPropName);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo(existingPropName);
        assertThat(actual.value()).isEqualTo(existingPropValue);
        assertThat(actual.stringValue()).isEqualTo(existingPropValue);
    }

    @Test
    @DisplayName("if system environment property exists, then should return it")
    void if_system_environment_property_exists_then_should_return_it() {
        var fakeProps = new FakeProps();
        var subject = new RandomizeIfEmptyProps(fakeProps);
        fakeProps.addEnv(existingPropName, existingPropValue);

        var actual = subject.env(existingPropName);

        assertThat(actual).isNotNull();
        assertThat(actual.name()).isEqualTo(existingPropName);
        assertThat(actual.value()).isEqualTo(existingPropValue);
        assertThat(actual.stringValue()).isEqualTo(existingPropValue);
    }
}
