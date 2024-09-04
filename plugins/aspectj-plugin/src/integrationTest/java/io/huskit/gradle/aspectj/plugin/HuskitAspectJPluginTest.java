package io.huskit.gradle.aspectj.plugin;

import io.huskit.gradle.commontest.BaseGradleIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitAspectJPluginTest extends BaseGradleIntegrationTest {

    @Test
    @DisplayName("plugin should be applied")
    void test_0() {
        var project = setupProject();

        project.getPlugins().apply(HuskitAspectJPlugin.class);

        assertThat(project.getPlugins().hasPlugin(HuskitAspectJPlugin.class)).isTrue();
    }
}
