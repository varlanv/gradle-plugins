package io.huskit.gradle.commontest;

import lombok.SneakyThrows;
import lombok.experimental.NonFinal;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.GradleVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.provider.Arguments;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import org.testcontainers.shaded.org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Tag("functional-test")
public abstract class BaseFunctionalTest extends BaseTest {

    @TempDir
    protected @NonFinal File rootTestProjectDir;
    protected @NonFinal File subjectProjectDir;
    protected @NonFinal File settingsFile;
    protected @NonFinal File rootBuildFile;
    protected @NonFinal File propertiesFile;
    protected static File huskitProjectRoot;

    @BeforeAll
    static void setupSpecParent() {
        huskitProjectRoot = findDirContaining(file -> file.getName().equals("internal-convention-plugin"));
    }

    @BeforeEach
    void setupParent() {
        subjectProjectDir = new File(rootTestProjectDir, "test-subject-project");
        if (!subjectProjectDir.exists()) {
            subjectProjectDir.mkdirs();
        }
    }

    @SneakyThrows
    protected void setupFixture() {
        settingsFile = new File(subjectProjectDir, "settings.gradle");
        rootBuildFile = new File(subjectProjectDir, "build.gradle");
        propertiesFile = new File(subjectProjectDir, "gradle.properties");
        FileUtils.write(propertiesFile, getPropertiesFileContent(), StandardCharsets.UTF_8);
        FileUtils.write(settingsFile, getSettingsFileContent(), StandardCharsets.UTF_8);
        FileUtils.write(rootBuildFile, getRootBuildFileContent(), StandardCharsets.UTF_8);
    }

    protected String getPropertiesFileContent() {
        return "";
    }

    protected String getSettingsFileContent() {
        return "";
    }

    protected String getRootBuildFileContent() {
        return "";
    }

    protected GradleRunner prepareGradleRunner(String taskName, DataTable params) {
        return prepareGradleRunner(params, taskName);
    }

    protected GradleRunner prepareGradleRunner(DataTable params, String... arguments) {
        List<String> argumentsList = new ArrayList<>(Arrays.asList(arguments));
        if (params.configurationCache()) {
            argumentsList.add("--configuration-cache");
        }
        if (params.buildCache()) {
            argumentsList.add("--build-cache");
        }
        return GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(subjectProjectDir)
                .withEnvironment(params.isCi() ? Map.of("CI", "true") : Map.of("CI", "false"))
                .withArguments(argumentsList)
                .forwardOutput()
                .withGradleVersion(params.gradleVersion());
    }

    protected BuildResult build(GradleRunner runner) {
        var lineStart = "*".repeat(215);
        var lineEnd = "*".repeat(161);
        var mark = "*".repeat(40);
        System.err.println();
        System.err.println(lineStart);
        System.err.println();
        System.err.printf("%s STARTING GRADLE FUNCTIONAL TEST BUILD FOR SPEC %s. LOGS BELOW ARE COMMING FROM GRADLE BUILD UNDER TEST %s%n", mark, getClass().getSimpleName(), mark);
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

    protected void verifyConfigurationCacheNotStored(BuildResult buildResult, String gradleVersion) {
        if (GradleVersion.version(gradleVersion).compareTo(GradleVersion.version("8.0")) >= 0) {
            assert buildResult.getOutput().contains("Configuration cache entry discarded because incompatible task was found:");
        } else {
            assert buildResult.getOutput().contains("Calculating task graph as no configuration cache is available for tasks:");
        }
    }

    @SneakyThrows
    protected String getRelativePath(File base, File file) {
        var basePath = base.getCanonicalPath();
        var filePath = file.getCanonicalPath();

        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1); // +1 to remove the trailing slash
        } else {
            return null;
        }
    }

    @SneakyThrows
    protected void copyFile(File source, File dest) {
        FileUtils.copyFile(source, dest);
    }

    protected void copyFolderContents(String srcDirPath, String destDirPath) {
        copyFolderContents(new File(srcDirPath), new File(destDirPath));
    }

    protected void copyFolderContents(File srcDir, File destDir) {
        if (!srcDir.exists()) {
            throw new IllegalArgumentException(String.format("Cannot copy from non-existing directory '%s'!", srcDir.getAbsolutePath()));
        }
        if (!srcDir.isDirectory()) {
            throw new IllegalArgumentException(String.format("Cannot copy from non-directory '%s'!", srcDir.getAbsolutePath()));
        }

        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        /*
        *   srcDir.eachFileRecurse { file ->
            def relativePath = getRelativePath(srcDir, file)
            def destFile = new File(destDir, relativePath)
            if (file.isDirectory()) {
                destFile.mkdirs()
            } else {
                copyFile(file, destFile)
            }
        }

        * */

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

    protected static File useCasesDir() {
        return findDirContaining(file -> file.getName().equals("use-cases"));
    }

    protected static File findDir(String dirName) {
        return findDir(file -> file.getName().equals(dirName));
    }

    protected static File findDirContaining(Predicate<File> predicate) {
        return findDir(file -> Arrays.stream(Objects.requireNonNull(file.listFiles())).anyMatch(predicate));
    }

    @SneakyThrows
    protected static File findDir(Predicate<File> predicate) {
        return findDir(predicate, new File("").getCanonicalFile());
    }

    protected static File findDir(Predicate<File> predicate, File current) {
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
    protected static void setFileText(File file, String text) {
        FileUtils.write(file, text, StandardCharsets.UTF_8);
    }

    public static Stream<Arguments> defaultDataTables() {
        return DataTables.getDefault().list().stream()
                .map(Arguments::of);
    }
}
