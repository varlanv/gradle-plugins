package io.huskit.gradle.plugin.internal;

import io.huskit.gradle.plugin.HuskitInternalConventionExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.testing.base.TestingExtension;

@RequiredArgsConstructor
public class ConfigureTests {

    private final ExtensionContainer extensions;
    private final HuskitInternalConventionExtension huskitConventionExtension;
    private final ConfigurationContainer configurations;
    private final PluginManager pluginManager;
    private final TaskContainer tasks;

    public void configure() {
        pluginManager.withPlugin("groovy", plugin -> {
            var testing = (TestingExtension) extensions.getByName("testing");
            var suites = testing.getSuites();
            var integrationTestTaskName = huskitConventionExtension.getIntegrationTestName().get();
            var integrationTestSuite = suites.register(
                    integrationTestTaskName,
                    JvmTestSuite.class,
                    suite -> {
//                        suite.getTargets().all(target -> {
//                            target.getTestTask().configure(test -> {
//                                test.getOutputs().upToDateWhen(task -> false);
//                            });
//                        });
                    });
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
                        });
                    });
                }
            });
            tasks.named("check", task -> task.dependsOn(integrationTestSuite));
            configurations.named(integrationTestTaskName + "Implementation", configuration -> {
                configuration.extendsFrom(configurations.getByName("testImplementation"));
            });
            configurations.named(integrationTestTaskName + "AnnotationProcessor", configuration -> {
                configuration.extendsFrom(configurations.getByName("testAnnotationProcessor"));
            });
            configurations.named(integrationTestTaskName + "CompileOnly", configuration -> {
                configuration.extendsFrom(configurations.getByName("testCompileOnly"));
            });
            configurations.named(integrationTestTaskName + "RuntimeOnly", configuration -> {
                configuration.extendsFrom(configurations.getByName("testRuntimeOnly"));
            });
        });
    }
}
