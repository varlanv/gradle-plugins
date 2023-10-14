package io.huskit.gradle.containers.plugin

import io.huskit.gradle.commontest.BaseFunctionalSpec
import io.huskit.gradle.commontest.DataTables
import io.huskit.gradle.containers.plugin.api.ContainersExtension
import spock.lang.Subject

@Subject(HuskitContainersPlugin)
class HuskitContainersPluginFunctionalSpec extends BaseFunctionalSpec {

    def "should add 'serviceContainers' extension"() {
        given:
        setupFixture()
        def runner = prepareGradleRunner(dataTable, "help", "--info")
        rootBuildFile.text = """
        plugins {
            id "io.huskit.gradle.containers-plugin"
        }
"""

        when:
        def buildResult = build(runner)

        then:
        buildResult.output.contains(ContainersExtension.name())

        where:
        dataTable << DataTables.default.get()
    }

    def "apply-plugin-to-single-java-project-gradle8 should work correctly"() {
        given:
        def testCaseDir = new File(new File(new File(useCasesDir(), "plugins"), "containers-plugin"), "apply-plugin-to-single-java-project-gradle8")
        copyFolderContents(testCaseDir.absolutePath, testProjectDir.absolutePath)
        def runner = prepareGradleRunner(dataTable, "clean", "check")
                .withEnvironment(["FUNCTIONAL_SPEC_RUN": 'true'])

        expect:
        runner.build()
        runner.build()

        where:
        dataTable << DataTables.default.get()
    }
}
