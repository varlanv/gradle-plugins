package io.huskit.gradle.plugin.internal;

import io.huskit.gradle.plugin.HuskitInternalConventionExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.quality.*;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.VariantVersionMappingStrategy;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.services.BuildServiceRegistry;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.jvm.toolchain.JvmVendorSpec;
import org.gradle.plugin.devel.plugins.JavaGradlePluginPlugin;
import org.gradle.testing.base.TestingExtension;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ApplyInternalPluginLogic {

    String projectPath;
    PluginManager pluginManager;
    RepositoryHandler repositories;
    DependencyHandler dependencies;
    ExtensionContainer extensions;
    HuskitInternalConventionExtension huskitConventionExtension;
    SoftwareComponentContainer components;
    TaskContainer tasks;
    ConfigurationContainer configurations;
    InternalEnvironment internalEnvironment;
    InternalProperties properties;
    String projectName;
    BuildServiceRegistry services;
    ProjectLayout projectLayout;
    File rootDir;
    Consumer<Runnable> afterEvaluate;

    @SuppressWarnings("UnstableApiUsage")
    public void apply() {
        var isGradlePlugin = projectPath.startsWith(":plugins") && projectPath.endsWith("-plugin");
        var targetJavaVersion = 17;

        // Apply common plugins
        if (isGradlePlugin) {
            pluginManager.apply(JavaGradlePluginPlugin.class);
            pluginManager.apply(JavaPlugin.class);
            pluginManager.apply(MavenPublishPlugin.class);
        }
        pluginManager.withPlugin("java", ignore -> {
            pluginManager.apply(PmdPlugin.class);
            pluginManager.apply(CheckstylePlugin.class);
        });

        // Configure repositories
        if (internalEnvironment.isLocal()) {
            repositories.add(repositories.mavenLocal());
        }
        repositories.add(repositories.mavenCentral());

        // Configure Java
        pluginManager.withPlugin("java", plugin -> {
            var java = (JavaPluginExtension) extensions.getByName("java");
            java.withSourcesJar();
            if (internalEnvironment.isCi()) {
                java.setSourceCompatibility(JavaLanguageVersion.of(targetJavaVersion));
                java.setTargetCompatibility(JavaLanguageVersion.of(targetJavaVersion));
            } else {
                java.toolchain(toolchain -> {
                    toolchain.getVendor().set(JvmVendorSpec.AZUL);
                    toolchain.getLanguageVersion().set(JavaLanguageVersion.of(targetJavaVersion));
                });
            }
        });

        // Add common dependencies
        pluginManager.withPlugin("java", plugin -> {
            var jetbrainsAnnotations = properties.getLib("jetbrains-annotations");
            dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, jetbrainsAnnotations);
            dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, jetbrainsAnnotations);
            if (!internalEnvironment.isTest() && !projectPath.equals(":common-test")) {
                dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, dependencies.project(Map.of("path", ":common-test")));
            }
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("assertj-core"));
            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("junit-jupiter-api"));
//            dependencies.add(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("jackson-core-databind"));
            dependencies.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, properties.getLib("junit-platform-launcher"));

            var lombokDependency = properties.getLib("lombok");
            dependencies.add(JavaPlugin.COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME, lombokDependency);
            dependencies.add(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME, lombokDependency);
        });

        if (projectPath.equals(":common-test")) {
            dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("assertj-core"));
//            dependencies.add(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME, properties.getLib("jackson-core-databind"));
        }

        // Configure build services
        var syncBuildService = services.registerIfAbsent(
                TestSynchronizerBuildService.name,
                TestSynchronizerBuildService.class,
                spec -> {
                }
        );

        var javaToolchainService = extensions.getByType(JavaToolchainService.class);

        afterEvaluate.accept(() -> {
            // Configure tests
            pluginManager.withPlugin("java", plugin -> {
                var testing = (TestingExtension) extensions.getByName("testing");
                var suites = testing.getSuites();
                var integrationTestTaskName = huskitConventionExtension.getIntegrationTestName().get();
                var integrationTestSuite = suites.register(
                        integrationTestTaskName,
                        JvmTestSuite.class,
                        suite ->
                                suite.getTargets().all(target ->
                                        target.getTestTask().configure(test ->
                                                test.getJavaLauncher().set(javaToolchainService.launcherFor(config -> {
                                                    config.getLanguageVersion().set(JavaLanguageVersion.of(17));
                                                    config.getVendor().set(JvmVendorSpec.AZUL);
                                                })))));
                suites.configureEach(suite -> {
                    if (suite instanceof JvmTestSuite) {
                        var jvmTestSuite = (JvmTestSuite) suite;
                        jvmTestSuite.useJUnitJupiter();
                        jvmTestSuite.dependencies(jvmComponentDependencies -> {
                            var implementation = jvmComponentDependencies.getImplementation();
                            implementation.add(jvmComponentDependencies.project());
                        });
                        jvmTestSuite.getTargets().all(target -> {
                            target.getTestTask().configure(test -> {
                                test.getOutputs().upToDateWhen(task -> false);
                                test.testLogging(logging -> {
                                    logging.setShowStandardStreams(true);
                                    logging.setShowStackTraces(true);
                                });
                                test.setFailFast(internalEnvironment.isCi());
                                test.usesService(syncBuildService);
                                test.doFirst(new ConfigureOnBeforeTestStart(syncBuildService));
                                var environment = new HashMap<>(test.getEnvironment());
                                environment.put("TESTCONTAINERS_REUSE_ENABLE", "true");
                                test.setEnvironment(environment);
                                var memory = test.getName().equals(JavaPlugin.TEST_TASK_NAME) ? "128m" : "512m";
//                                test.systemProperty("junit.jupiter.execution.parallel.enabled", internalEnvironment.isLocal());
                                test.setJvmArgs(
                                        Stream.of(
                                                        test.getName().equals("functionalTest") ?
                                                                List.of() :
                                                                List.of("-Xms" + memory, "-Xmx" + memory),
                                                        test.getName().equals("functionalTest") ?
                                                                List.of() :
                                                                List.of(/*"-XX:+UnlockExperimentalVMOptions", "-XX:+UseEpsilonGC"*/),
                                                        test.getJvmArgs(),
                                                        Arrays.asList(
                                                                "-XX:TieredStopAtLevel=1",
                                                                "-noverify"
//                                                                test.getName().equals(JavaPlugin.TEST_TASK_NAME) ? "-Xms32m" : "-Xms128m",
//                                                                "-XX:+UseParallelGC",
//                                                                "-XX:ParallelGCThreads=2"
                                                        )
                                                )
                                                .filter(Objects::nonNull)
                                                .flatMap(Collection::stream)
                                                .collect(Collectors.toList())
                                );
                            });
                        });
                    }
                });
                tasks.named("check", task -> task.dependsOn(integrationTestSuite));

                // configure integration test configurations
                configurations.named(integrationTestTaskName + "Implementation", configuration -> {
                    configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME));
                });
                configurations.named(integrationTestTaskName + "AnnotationProcessor", configuration -> {
                    configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME));
                });
                configurations.named(integrationTestTaskName + "CompileOnly", configuration -> {
                    configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_COMPILE_ONLY_CONFIGURATION_NAME));
                });
                configurations.named(integrationTestTaskName + "RuntimeOnly", configuration -> {
                    configuration.extendsFrom(configurations.getByName(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME));
                });
            });

            // Configure publishing
            pluginManager.withPlugin("maven-publish", plugin -> {
                extensions.configure(PublishingExtension.class, publishingExtension -> {
                    publishingExtension.getPublications().create("mavenJava", MavenPublication.class, mavenPublication -> {
                        SoftwareComponent javaComponent = components.getByName("java");
                        mavenPublication.from(javaComponent);
                        mavenPublication.versionMapping(versionMappingStrategy -> {
                            versionMappingStrategy.usage("java-api", variantVersionMappingStrategy ->
                                    variantVersionMappingStrategy.fromResolutionOf(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));
                            versionMappingStrategy.usage("java-runtime", VariantVersionMappingStrategy::fromResolutionResult);
                        });
                    });
                });
            });

            // Configure static analysis
            // Configure aggregate static analysis tasks
            var staticAnalyseMain = tasks.register("staticAnalyseMain", task -> {
                task.setGroup("static analysis");
                task.setDescription("Run static analysis on main sources");
            });
            var staticAnalyseTest = tasks.register("staticAnalyseTest", task -> {
                task.setGroup("static analysis");
                task.setDescription("Run static analysis on test sources");
            });
            tasks.register("staticAnalyseFull", task -> {
                task.setGroup("static analysis");
                task.setDescription("Run static analysis on all sources");
                task.dependsOn(staticAnalyseMain, staticAnalyseTest);
            });

            tasks.named("check", task -> {
                task.dependsOn(staticAnalyseMain);
                task.dependsOn(staticAnalyseTest);
            });

            // Configure pmd
            pluginManager.withPlugin("pmd", pmd -> {
                var pmdExtension = (PmdExtension) extensions.getByName("pmd");
                pmdExtension.setRuleSetFiles(projectLayout.files(Paths.get(rootDir.getAbsolutePath(), "static-analyse", "pmd.xml")));
                pmdExtension.setToolVersion("7.5.0");
                var pmdMainTask = tasks.named("pmdMain", Pmd.class, pmdTask -> {
                    pmdTask.setRuleSetFiles(projectLayout.files(Paths.get(rootDir.getAbsolutePath(), "static-analyse", "pmd.xml")));
                });
                var pmdTestTasks = Stream.of("test", huskitConventionExtension.getIntegrationTestName().get())
                        .map(string -> "pmd" + string.substring(0, 1).toUpperCase() + string.substring(1))
                        .map(taskName -> tasks.named(taskName, Pmd.class, pmdTask -> {
                            pmdTask.setRuleSetFiles(projectLayout.files(Paths.get(rootDir.getAbsolutePath(), "static-analyse", "pmd-test.xml")));
                        }))
                        .collect(Collectors.toList());
                staticAnalyseMain.configure(task -> task.dependsOn(pmdMainTask));
                staticAnalyseTest.configure(task -> task.dependsOn(pmdTestTasks));
            });

            // Configure checkstyle
            pluginManager.withPlugin("checkstyle", checkstyle -> {
                var checkstyleExtension = extensions.getByType(CheckstyleExtension.class);
                checkstyleExtension.setToolVersion("10.18.1");
                checkstyleExtension.setMaxWarnings(0);
                checkstyleExtension.setMaxErrors(0);
                checkstyleExtension.setConfigFile(Paths.get(rootDir.getAbsolutePath(), "static-analyse", "checkstyle.xml").toFile());

                var checkstyleMainTask = tasks.named("checkstyleMain");
                var checkstyleTestTasks = Stream.of("test", huskitConventionExtension.getIntegrationTestName().get())
                        .map(string -> "checkstyle" + string.substring(0, 1).toUpperCase() + string.substring(1))
                        .map(taskName -> tasks.named(taskName, Task.class))
                        .collect(Collectors.toList());

                staticAnalyseMain.configure(task -> task.dependsOn(checkstyleMainTask));
                staticAnalyseTest.configure(task -> task.dependsOn(checkstyleTestTasks));
            });
        });
    }
}
