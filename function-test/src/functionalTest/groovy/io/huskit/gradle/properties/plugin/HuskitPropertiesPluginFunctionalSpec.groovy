package io.huskit.gradle.properties.plugin

import io.huskit.gradle.common.plugin.model.props.Props
import io.huskit.gradle.commontest.BaseFunctionalSpec
import io.huskit.gradle.commontest.DataTables
import spock.lang.PendingFeature
import spock.lang.Subject

@Subject(HuskitPropertiesPlugin)
class HuskitPropertiesPluginFunctionalSpec extends BaseFunctionalSpec {

    def "if extension didn't exist before, then should add extension"() {
        given:
        setupFixture()
        def runner = prepareGradleRunner(dataTable, "help", "--info").forwardOutput()
        rootBuildFile.text = """
        plugins {
            id "io.huskit.gradle.properties-plugin"
        }
"""

        when:
        def buildResult = runner.build()

        then:
        buildResult.output.contains("Added extension: ${Props.EXTENSION_NAME}")

        where:
        dataTable << DataTables.default.get()
    }

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
    }
}
