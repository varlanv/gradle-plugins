package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.Props;
import io.huskit.gradle.common.plugin.model.props.fake.FakeProps;
import io.huskit.gradle.commontest.GradleIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitPropertiesPluginIntegrationTest implements GradleIntegrationTest {

    @Test
    @DisplayName("plugin should be applied")
    void plugin_should_be_applied() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getPlugins().apply(HuskitPropertiesPlugin.class);

            assertThat(project.getPlugins().hasPlugin(HuskitPropertiesPlugin.class)).isTrue();
        });
    }

    @Test
    @DisplayName("should add props extension if not already exists")
    void should_add_props_extension_if_not_already_exists() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            assertThat(project.getExtensions().findByName(Props.name())).isNull();

            project.getPlugins().apply(HuskitPropertiesPlugin.class);

            assertThat(project.getExtensions().findByName(Props.name())).isNotNull();
        });
    }

    @Test
    @DisplayName("should use existing props extension if already exists")
    void should_use_existing_props_extension_if_already_exists() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            var fakeProps = new FakeProps();
            project.getExtensions().add(Props.class, Props.name(), fakeProps);

            project.getPlugins().apply(HuskitPropertiesPlugin.class);

            assertThat(project.getExtensions().findByName(Props.name())).isEqualTo(fakeProps);
        });
    }
}
