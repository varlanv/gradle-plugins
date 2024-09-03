package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.common.plugin.model.props.exception.NonNullPropertyException;
import io.huskit.gradle.commontest.BaseStatelessUnitTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class FakeNonNullPropTest extends BaseStatelessUnitTest {

    String propName = "propName";
    String propValue = "propVal";

    @Test
    @DisplayName("'name' should return param")
    void test_0() {
        assertThat(new FakeNonNullProp(propName, propValue).name()).isEqualTo(propName);
    }

    @Test
    @DisplayName("'value' if value non null should return param")
    void test_1() {
        assertThat(new FakeNonNullProp(propName, propValue).value()).isEqualTo(propValue);
    }

    @Test
    @DisplayName("'stringValue' if value non null should return param")
    void test_2() {
        assertThat(new FakeNonNullProp(propName, propValue).stringValue()).isEqualTo(propValue);
    }

    @Test
    @DisplayName("'value' if value null then throw exception")
    void test_3() {
        assertThatThrownBy(() -> new FakeNonNullProp(propName, null).value())
                .isInstanceOf(NonNullPropertyException.class)
                .hasMessageContaining(propName);
    }

    @Test
    @DisplayName("'stringValue' if value null then throw exception")
    void test_4() {
        assertThatThrownBy(() -> new FakeNonNullProp(propName, null).stringValue())
                .isInstanceOf(NonNullPropertyException.class)
                .hasMessageContaining(propName);
    }
}
