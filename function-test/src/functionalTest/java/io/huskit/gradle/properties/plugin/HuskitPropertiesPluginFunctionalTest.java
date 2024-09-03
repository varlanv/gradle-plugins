package io.huskit.gradle.properties.plugin;

import io.huskit.gradle.common.plugin.model.props.Props;
import io.huskit.gradle.commontest.BaseFunctionalTest;
import io.huskit.gradle.commontest.DataTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitPropertiesPluginFunctionalTest extends BaseFunctionalTest {

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("if extension didn't exist before, then should add extension")
    void test_0(DataTable dataTable) {
        setupFixture();
        var runner = prepareGradleRunner(dataTable, "help", "--info").forwardOutput();
        setFileText(rootBuildFile, "plugins { id 'io.huskit.gradle.properties-plugin' }");

        var buildResult = runner.build();

        assertThat(buildResult.getOutput()).contains(String.format("Added extension: %s", Props.EXTENSION_NAME));
    }

/*
    @PendingFeature
    def "if extension existed before, then should keep it"() {
        given:
        setupFixture()
        def runner = prepareGradleRunner(dataTable, "help", "--info").forwardOutput()
        rootBuildFile.text = """
"""

        when:
        def buildResult = runner.build()

        then:
        buildResult.output.contains("Added extension: ${Props.EXTENSION_NAME}")

        where:
        dataTable << DataTables.default.get()
    }*/
}
