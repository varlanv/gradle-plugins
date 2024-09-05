package io.huskit.gradle.commontest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.provider.Arguments;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Tags({@Tag(BaseTest.FUNCTIONAL_TEST_TAG), @Tag(BaseTest.SLOW_TEST_TAG)})
public interface FunctionalTest extends BaseTest {

    default File huskitProjectRoot() {
        return TestUtils.huskitProjectRoot();
    }

    @BeforeAll
    default void setupFunctionalSpec() {
        TestUtils.setHuskitProjectRoot(() -> findDirContaining(file -> file.getName().equals("internal-convention-plugin")));
    }

    @SneakyThrows
    default void runFunctionalFixture(ThrowingConsumer<FunctionalFixture> fixtureConsumer) {
        var rootTestProjectDir = newTempDir();
        runAndDeleteFile(rootTestProjectDir, () -> {
            var subjectProjectDir = new File(rootTestProjectDir, "test-subject-project");
            subjectProjectDir.mkdir();
            var settingsFile = new File(subjectProjectDir, "settings.gradle");
            var rootBuildFile = new File(subjectProjectDir, "build.gradle");
            var propertiesFile = new File(subjectProjectDir, "gradle.properties");
            fixtureConsumer.accept(
                    new FunctionalFixture(
                            rootTestProjectDir,
                            subjectProjectDir,
                            settingsFile,
                            rootBuildFile,
                            propertiesFile
                    )
            );
        });
    }

    default void runGradleRunnerFixture(DataTable params, String taskName, ThrowingConsumer<RunnerFunctionalFixture> fixtureConsumer) {
        runGradleRunnerFixture(params, List.of(taskName), fixtureConsumer);
    }

    default void runGradleRunnerFixture(DataTable params, List<String> arguments, ThrowingConsumer<RunnerFunctionalFixture> fixtureConsumer) {
        runFunctionalFixture(parentFixture -> {
            var args = new ArrayList<>(arguments);
            if (params.configurationCache()) {
                args.add("--configuration-cache");
            }
            if (params.buildCache()) {
                args.add("--build-cache");
            }
            args.add("--warning-mode=summary");
            args.add("-Dorg.gradle.logging.level=lifecycle");
            fixtureConsumer.accept(
                    new RunnerFunctionalFixture(
                            GradleRunner.create()
                                    .withPluginClasspath()
                                    .withProjectDir(parentFixture.subjectProjectDir())
                                    .withEnvironment(params.isCi() ? Map.of("CI", "true") : Map.of("CI", "false"))
                                    .withArguments(args)
                                    .forwardOutput()
                                    .withGradleVersion(params.gradleVersion()),
                            parentFixture.rootTestProjectDir(),
                            parentFixture.subjectProjectDir(),
                            parentFixture.settingsFile(),
                            parentFixture.rootBuildFile(),
                            parentFixture.propertiesFile()
                    )
            );
        });
    }

    default BuildResult build(GradleRunner runner) {
        var lineStart = "*".repeat(215);
        var lineEnd = "*".repeat(161);
        var mark = "*".repeat(40);
        System.err.println();
        System.err.println(lineStart);
        System.err.println();
        System.err.printf("%s STARTING GRADLE FUNCTIONAL TEST BUILD FOR SPEC %s. LOGS BELOW ARE COMMING FROM GRADLE BUILD UNDER TEST %s%n", mark, getClass().getSimpleName(), mark);
        System.err.printf("Gradle build args: %s%n", String.join(" ", runner.getArguments()));
        System.err.printf("Java version - %s%n", System.getProperty("java.version"));
        System.err.println();
        System.err.println(lineStart);
        System.err.println();
        try {
            return runner.build();
        } finally {
            System.err.println();
            System.err.println(lineEnd);
            System.err.println();
            System.err.printf("%s FINISHED GRADLE FUNCTIONAL TEST BUILD FOR %s %s%n", mark, getClass().getSimpleName(), mark);
            System.err.println();
            System.err.println(lineEnd);
            System.err.println();
        }
    }

    default void verifyConfigurationCacheNotStored(BuildResult buildResult, String gradleVersion) {
        if (GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("8.0")) >= 0) {
            assert buildResult.getOutput().contains("Configuration cache entry discarded because incompatible task was found:");
        } else {
            assert buildResult.getOutput().contains("Calculating task graph as no configuration cache is available for tasks:");
        }
    }

    @SneakyThrows
    default String getRelativePath(File base, File file) {
        var basePath = base.getCanonicalPath();
        var filePath = file.getCanonicalPath();

        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1); // +1 to remove the trailing slash
        } else {
            return null;
        }
    }

    @SneakyThrows
    default void copyFile(File source, File dest) {
        FileUtils.copyFile(source, dest);
    }

    default void copyFolderContents(String srcDirPath, String destDirPath) {
        copyFolderContents(new File(srcDirPath), new File(destDirPath));
    }

    default void copyFolderContents(File srcDir, File destDir) {
        if (!srcDir.exists()) {
            throw new IllegalArgumentException(String.format("Cannot copy from non-existing directory '%s'!", srcDir.getAbsolutePath()));
        }
        if (!srcDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Cannot copy from non-directory '%s'!", srcDir.getAbsolutePath()));
        }

        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        FileUtils.listFilesAndDirs(srcDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
                .forEach(file -> {
                    if (!file.getAbsolutePath().equals(srcDir.getAbsolutePath())) {
                        var relativePath = getRelativePath(srcDir, file);
                        var destFile = new File(destDir, relativePath);
                        if (file.isDirectory()) {
                            destFile.mkdirs();
                        } else {
                            copyFile(file, destFile);
                        }
                    }
                });
    }

    default File useCasesDir() {
        return findDirContaining(file -> file.getName().equals("use-cases"));
    }

    default File findDir(String dirName) {
        return findDir(file -> file.getName().equals(dirName));
    }

    default File findDirContaining(Predicate<File> predicate) {
        return findDir(file -> Arrays.stream(Objects.requireNonNull(file.listFiles())).anyMatch(predicate));
    }

    @SneakyThrows
    default File findDir(Predicate<File> predicate) {
        return findDir(predicate, new File("").getCanonicalFile());
    }

    default File findDir(Predicate<File> predicate, File current) {
        if (predicate.test(current)) {
            return current;
        } else {
            var parentFile = current.getParentFile();
            if (parentFile == null) {
                throw new RuntimeException(String.format("Cannot find directory with predicate: %s", predicate));
            }
            return findDir(predicate, parentFile);
        }
    }

    @SneakyThrows
    default void setFileText(File file, String text) {
        FileUtils.write(file, text, StandardCharsets.UTF_8);
    }

    default Stream<Arguments> defaultDataTables() {
        return DataTables.getDefault().list().stream()
                .map(Arguments::of);
    }

    @Getter
    @RequiredArgsConstructor
    class FunctionalFixture {

        File rootTestProjectDir;
        File subjectProjectDir;
        File settingsFile;
        File rootBuildFile;
        File propertiesFile;
    }

    @Getter
    @RequiredArgsConstructor
    class RunnerFunctionalFixture {

        GradleRunner runner;
        File rootTestProjectDir;
        File subjectProjectDir;
        File settingsFile;
        File rootBuildFile;
        File propertiesFile;
    }
}
