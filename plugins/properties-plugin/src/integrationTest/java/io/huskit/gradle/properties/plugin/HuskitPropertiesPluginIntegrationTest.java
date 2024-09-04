package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.Props;
import io.huskit.gradle.common.plugin.model.props.fake.FakeProps;
import io.huskit.gradle.commontest.BaseGradleIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitPropertiesPluginIntegrationTest extends BaseGradleIntegrationTest {

    @Test
    @DisplayName("plugin should be applied")
    void test_0() {
        var project = setupProject();

        project.getPlugins().apply(HuskitPropertiesPlugin.class);

        assertThat(project.getPlugins().hasPlugin(HuskitPropertiesPlugin.class)).isTrue();
    }

    @Test
    @DisplayName("should add props extension if not already exists")
    void test_1() {
        var project = setupProject();
        assertThat(project.getExtensions().findByName(Props.EXTENSION_NAME)).isNull();

        project.getPlugins().apply(HuskitPropertiesPlugin.class);

        assertThat(project.getExtensions().findByName(Props.EXTENSION_NAME)).isNotNull();
    }

    @Test
    @DisplayName("should use existing props extension if already exists")
    void test_2() {
        var project = setupProject();
        var fakeProps = new FakeProps();
        project.getExtensions().add(Props.class, Props.EXTENSION_NAME, fakeProps);

        project.getPlugins().apply(HuskitPropertiesPlugin.class);

        assertThat(project.getExtensions().findByName(Props.EXTENSION_NAME)).isEqualTo(fakeProps);
    }
}
