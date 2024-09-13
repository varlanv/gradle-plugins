package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.Props;
import io.huskit.gradle.commontest.DataTable;
import io.huskit.gradle.commontest.FunctionalTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitPropertiesPluginFunctionalTest implements FunctionalTest {

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("if extension didn't exist before, then should add extension")
    void if_extension_didnt_exist_before_then_should_add_extension(DataTable dataTable) {
        runGradleRunnerFixture(
                dataTable,
                List.of("help"),
                fixture -> {
                    setFileText(fixture.rootBuildFile(), "plugins { id 'io.huskit.gradle.properties-plugin' }");

                    var buildResult = fixture.runner().build();

                    assertThat(buildResult.getOutput()).contains(String.format("Added extension: [%s]", Props.name()));
                });
    }
}
