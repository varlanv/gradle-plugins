package io.huskit.gradle.containers.plugin

import io.huskit.gradle.GradleRunResult
import io.huskit.gradle.commontest.BaseDockerFunctionalSpec
import io.huskit.gradle.commontest.DataTable
import io.huskit.gradle.commontest.DataTables
import io.huskit.gradle.containers.plugin.api.ContainersExtension
import spock.lang.Subject

@Subject(HuskitContainersPlugin)
class HuskitContainersPluginFunctionalSpec extends BaseDockerFunctionalSpec {

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

    def "apply-plugin-to-multiple-java-projects-all-not-reusable should work correctly"() {
        when:
        def result = runUseCase("apply-plugin-to-multiple-java-projects-all-not-reusable-gradle8", dataTable)

        then: "Output should contain all expected messages"
        def messages = result.findMarkedMessages("MONGO_CONNECTION_STRING").values()

        and: "There are 6 unique mongo connection strings"
        messages.size() == 6
        messages.toSet().size() == 6

        where:
        dataTable << DataTables.default.get()
    }

    def "apply-plugin-to-single-java-project should work correctly"() {
        expect:
        runUseCase("apply-plugin-to-single-java-project", dataTable)

        where:
        dataTable << DataTables.default.get()
    }

    GradleRunResult runUseCase(String useCaseName, DataTable dataTable) {
        def useCaseDir = useCaseDir(useCaseName)
        assert useCaseDir != null
        assert useCaseDir.exists()
        assert useCaseDir.isDirectory()
        copyFolderContents(useCaseDir.absolutePath, subjectProjectDir.absolutePath)
        copyFolderContents(useCasesCommonLogicDir().absolutePath, rootTestProjectDir.absolutePath)
        def runner = prepareGradleRunner(dataTable, "clean", "check")
                .withEnvironment(["FUNCTIONAL_SPEC_RUN": 'true'])

        def build1result = build(runner)
        def build2result = build(runner)
        return new GradleRunResult([build1result, build2result])
    }

    private File useCaseDir(String useCaseDirName) {
        return new File(
                new File(
                        new File(
                                useCasesDir(),
                                "plugins"
                        ),
                        "containers-plugin"
                ),
                useCaseDirName
        )
    }

    private File useCasesCommonLogicDir() {
        return new File(
                new File(
                        huskitProjectRoot,
                        "use-cases"
                ),
                "common-use-cases-logic"
        )
    }
}
