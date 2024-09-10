package io.huskit.gradle.aspectj.plugin;

import io.huskit.gradle.commontest.GradleIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitAspectjPluginTest implements GradleIntegrationTest {

    @Test
    @DisplayName("plugin should be applied")
    void test_0() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getPlugins().apply(HuskitAspectjPlugin.class);

            assertThat(project.getPlugins().hasPlugin(HuskitAspectjPlugin.class)).isTrue();
        });
    }
}
