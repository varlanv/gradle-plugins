package io.huskit.gradle.containers.plugin;

import io.huskit.gradle.BaseDockerFunctionalTest;
import io.huskit.gradle.GradleRunResult;
import io.huskit.gradle.commontest.DataTable;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitContainersPluginFunctionalTest extends BaseDockerFunctionalTest {

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("should add 'serviceContainers' extension")
    void test_0(DataTable dataTable) {
        setupFixture();
        var runner = prepareGradleRunner(dataTable, "help", "--info");
        setFileText(rootBuildFile, "plugins { id 'io.huskit.gradle.containers-plugin' }");
        var buildResult = build(runner);

        assertThat(buildResult.getOutput()).contains(ContainersExtension.name());
    }

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("apply-plugin-to-multiple-java-projects-all-not-reusable should work correctly")
    void test_1(DataTable dataTable) {
        var useCaseName = "apply-plugin-to-multiple-java-projects-all-not-reusable-gradle8";

        var result = runUseCase(useCaseName, dataTable);

        var messages = result.findMarkedMessages("MONGO_CONNECTION_STRING").values();
        assertThat(messages.size()).isEqualTo(6);
        assertThat(Set.copyOf(messages).size()).isEqualTo(6);
        assertThat(findHuskitContainersForUseCase(useCaseName).size()).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("apply-plugin-to-single-java-project should work correctly")
    void test_2(DataTable dataTable) {
        var useCaseName = "apply-plugin-to-single-java-project-gradle8";

        var result = runUseCase(useCaseName, dataTable);

        var messages = result.findMarkedMessages("MONGO_CONNECTION_STRING").values();
        assertThat(messages.size()).isEqualTo(2);
        assertThat(Set.copyOf(messages).size()).isEqualTo(2);
        assertThat(findHuskitContainersForUseCase(useCaseName).size()).isEqualTo(0);
    }

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("apply-plugin-to-multiple-java-projects should work correctly")
    void test_3(DataTable dataTable) {
        var useCaseName = "apply-plugin-to-multiple-java-projects-gradle8";

        var result = runUseCase(useCaseName, dataTable);

        var messages = result.findMarkedMessages("MONGO_CONNECTION_STRING").values();

        // Mongo containers were requested 6 times while only 1 unique connection string is used
        assertThat(messages.size()).isEqualTo(6);
        var mongoHosts = messages.stream()
                .map(it -> StringUtils.substringBefore(StringUtils.substringAfter(it, "mongodb://"), "/"))
                .collect(Collectors.toList());
        assertThat(Set.copyOf(mongoHosts)).hasSize(1);

        // Mongo container is still available
        var containers = findHuskitContainersForUseCase(useCaseName);
        assertThat(containers).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("defaultDataTables")
    @DisplayName("apply-plugin-to-multiple-java-projects-with-and-without-reuse should work correctly")
    void test_4(DataTable dataTable) {
        var useCaseName = "apply-plugin-to-multiple-java-projects-with-and-without-reuse-gradle8";

        var result = runUseCase(useCaseName, dataTable);

        var messages = result.findMarkedMessages("MONGO_CONNECTION_STRING").values();

        // Mongo containers were requested 6 times while only 3 unique connection strings are used
        assertThat(messages).hasSize(6);
        var mongoHosts = messages.stream()
                .map(it -> StringUtils.substringBefore(StringUtils.substringAfter(it, "mongodb://"), "/"))
                .collect(Collectors.toList());
        assertThat(Set.copyOf(mongoHosts)).hasSize(3);

        // Mongo container is still available
        var containers = findHuskitContainersForUseCase(useCaseName);
        assertThat(containers).hasSize(1);
    }

    private GradleRunResult runUseCase(String useCaseName, DataTable dataTable) {
        var useCaseDir = useCaseDir(useCaseName);
        assertThat(useCaseDir).isNotNull();
        assertThat(useCaseDir.exists()).isTrue();
        assertThat(useCaseDir.isDirectory()).isTrue();
        copyFolderContents(useCaseDir, subjectProjectDir);
        copyFolderContents(useCasesCommonLogicDir(), rootTestProjectDir);
        var runner = prepareGradleRunner(dataTable, "clean", "check")
                .withEnvironment(Map.of("FUNCTIONAL_SPEC_RUN", "true"));

        var build1result = build(runner);
        var build2result = build(runner);
        return new GradleRunResult(new ArrayList<>(List.of(build1result, build2result)));
    }

    private File useCaseDir(String useCaseDirName) {
        return new File(huskitProjectRoot.getAbsolutePath() + File.separator + "use-cases" + File.separator + "plugins" + File.separator + "containers-plugin" + File.separator + useCaseDirName);
    }

    private File useCasesCommonLogicDir() {
        return new File(huskitProjectRoot.getAbsolutePath() + File.separator + "use-cases" + File.separator + "common-use-cases-logic");
    }
}
