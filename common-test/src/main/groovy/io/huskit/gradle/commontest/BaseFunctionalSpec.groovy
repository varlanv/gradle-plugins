package io.huskit.gradle.commontest

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import spock.lang.Tag
import spock.lang.TempDir

@Tag("functional-test")
class BaseFunctionalSpec extends BaseSpec {

    @TempDir
    File testProjectDir
    File settingsFile
    File rootBuildFile
    File propertiesFile

    def setupFixture() {
        settingsFile = new File(testProjectDir, "settings.gradle")
        rootBuildFile = new File(testProjectDir, "build.gradle")
        propertiesFile = new File(testProjectDir, "gradle.properties")

        propertiesFile.text = getPropertiesFileContent()
        settingsFile.text = getSettingsFileContent()
        rootBuildFile.text = getRootBuildFileContent()
    }

    String getPropertiesFileContent() {
        return ""
    }

    String getSettingsFileContent() {
        return ""
    }

    String getRootBuildFileContent() {
        return ""
    }

    protected GradleRunner prepareGradleRunner(String taskName, DataTable params) {
        return prepareGradleRunner(params, taskName)
    }

    protected GradleRunner prepareGradleRunner(DataTable params, String... arguments) {
        List<String> argumentsList = arguments.toList()
        if (params.configurationCache) {
            argumentsList.add("--configuration-cache")
        }
        if (params.buildCache) {
            argumentsList.add("--build-cache")
        }
        return GradleRunner.create()
                .withPluginClasspath()
                .withProjectDir(testProjectDir)
                .withEnvironment(params.isCi ? ["CI": "true"] : ["CI": "false"])
                .withArguments(argumentsList)
                .forwardOutput()
                .withGradleVersion(params.gradleVersion)
    }

    protected BuildResult build(GradleRunner runner) {
        def lineStart = ("***********************************************************************************************************************************************************************************************************************") as String
        def lineEnd = ("*****************************************************************************************************************************************************************")
        def mark = ("*" * 40) as String
        System.err.println(lineStart)
        System.err.println()
        System.err.println("${mark} STARTING GRADLE FUNCTIONAL TEST BUILD FOR SPEC ${getClass().getSimpleName()}. LOGS BEFORE ARE COMMING FROM GRADLE BUILD UNDER TEST ${mark}")
        System.err.println("Java version - ${System.getProperty("java.version")}")
        System.err.println()
        System.err.println(lineStart)
        def result = runner.build()
        System.err.println(lineEnd)
        System.err.println()
        System.err.println("${mark} FINISHED GRADLE FUNCTIONAL TEST BUILD FOR SPEC ${getClass().getSimpleName()} ${mark}")
        System.err.println()
        System.err.println(lineEnd)
        return result
    }

    protected void verifyConfigurationCacheNotStored(BuildResult buildResult, String gradleVersion) {
        if (GradleVersion.version(gradleVersion) >= GradleVersion.version("8.0")) {
            assert buildResult.output.contains("Configuration cache entry discarded because incompatible task was found:")
        } else {
            assert buildResult.output.contains("Calculating task graph as no configuration cache is available for tasks:")
        }
    }

    protected String getRelativePath(File base, File file) {
        def basePath = base.canonicalPath
        def filePath = file.canonicalPath

        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1) // +1 to remove the trailing slash
        } else {
            return null
        }
    }

    protected void copyFile(source, dest) {
        source.withInputStream { input ->
            dest.withOutputStream { output ->
                output << input
            }
        }
    }

    protected void copyFolderContents(String srcDirPath, String destDirPath) {
        def srcDir = new File(srcDirPath)
        def destDir = new File(destDirPath)

        if (!srcDir.isDirectory()) {
            println "${srcDirPath} is not a directory!"
            return
        }

        if (!destDir.exists()) {
            destDir.mkdirs()
        }

        srcDir.eachFileRecurse { file ->
            def relativePath = getRelativePath(srcDir, file)
            def destFile = new File(destDir, relativePath)
            if (file.isDirectory()) {
                destFile.mkdirs()
            } else {
                copyFile(file, destFile)
            }
        }
    }

    protected File useCasesDir(File current = null) {
        if (current == null) {
            current = new File("").canonicalFile
        }
        def files = current.listFiles()
        def result = files.find { it.name == "use-cases" && it.isDirectory() }
        if (result != null) {
            return result
        } else {
            def parentFile = current.parentFile
            if (parentFile == null) {
                throw new RuntimeException("use-cases directory not found")
            }
            return useCasesDir(parentFile)
        }
    }
}
