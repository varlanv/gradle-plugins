package io.huskit.gradle.containers.plugin;

import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.gradle.commontest.GradleIntegrationTest;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.api.mongo.MongoContainerRequestSpec;
import io.huskit.gradle.containers.plugin.internal.*;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HuskitContainersPluginIntegrationTest implements GradleIntegrationTest {

    @Test
    @DisplayName("should create build service")
    void test_0() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            project.getPlugins().apply(HuskitContainersPlugin.class);

            var buildServices = new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
            assertThat(buildServices).hasSize(1);
            assertThat(buildServices.get(0).getName()).isEqualTo(ContainersBuildService.name());
        });
    }

    @Test
    @DisplayName("if build service already added, then no error thrown")
    void test_1() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getGradle().getSharedServices().registerIfAbsent(
                    ContainersBuildService.name(),
                    ContainersBuildService.class,
                    params -> {
                    }
            );
            project.getPlugins().apply(HuskitContainersPlugin.class);

            var buildServices = new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
            assertThat(buildServices).hasSize(1);
            assertThat(buildServices.get(0).getName()).isEqualTo(ContainersBuildService.name());
        });
    }

    @Test
    @DisplayName("if parent project already created build service, then no error thrown")
    void test_2() {
        runProjectWithParentFixture(fixture -> {
            fixture.parentProject().getPlugins().apply(HuskitContainersPlugin.class);
            fixture.project().getPlugins().apply(HuskitContainersPlugin.class);

            var projectBuildServices = new ArrayList<>(fixture.project().getGradle().getSharedServices().getRegistrations());
            var parentProjectBuildServices = new ArrayList<>(fixture.parentProject().getGradle().getSharedServices().getRegistrations());
            assertThat(projectBuildServices).hasSize(1);
            assertThat(parentProjectBuildServices).hasSize(1);
            assertThat(projectBuildServices.get(0)).isEqualTo(parentProjectBuildServices.get(0));
            assertThat(projectBuildServices.get(0).getName()).isEqualTo(ContainersBuildService.name());
        });
    }

    @Test
    @DisplayName("if parent project already created build service, and build service evaluated, then no error thrown")
    void test_3() {
        runProjectWithParentFixture(fixture -> {
            fixture.parentProject().getPlugins().apply(HuskitContainersPlugin.class);
            fixture.project().getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(fixture.parentProject());
            evaluateProject(fixture.project());
            var projectBuildServices = new ArrayList<>(fixture.project().getGradle().getSharedServices().getRegistrations());
            var parentProjectBuildServices = new ArrayList<>(fixture.parentProject().getGradle().getSharedServices().getRegistrations());
            var projectBuildService = projectBuildServices.get(0).getService().getOrNull();
            var parentProjectBuildService = parentProjectBuildServices.get(0).getService().getOrNull();

            assertThat(projectBuildService).isNotNull();
            assertThat(parentProjectBuildService).isNotNull();
            assertThat(projectBuildService).isEqualTo(parentProjectBuildService);
        });
    }

    @Test
    @DisplayName("when build service evaluated, no exception thrown")
    void test_4() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);
            var buildService = List.copyOf(project.getGradle().getSharedServices().getRegistrations()).get(0).getService().getOrNull();

            assertThat(buildService).isNotNull();
        });
    }

    @Test
    @DisplayName("if there is one project with plugin, then max parallel uses should be 1")
    void test_5() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getPlugins().apply(HuskitContainersPlugin.class);

            var maxParallelUsages = List.copyOf(project.getGradle().getSharedServices().getRegistrations()).get(0).getMaxParallelUsages();
            assertThat(maxParallelUsages.isPresent()).isTrue();
            assertThat(maxParallelUsages.get()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("if there are two projects with plugin, then max parallel uses should be 2")
    void test_6() {
        runMultiProjectContainerFixture(fixture -> {
            assertThat(fixture.maxParallelUsages.get()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("containers extension should be added")
    void test_7() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);

            assertThat(project.getExtensions().findByName(ContainersExtension.name())).isNotNull();
            assertThat(project.getExtensions().findByType(ContainersExtension.class)).isNotNull();
        });
    }

    @Test
    @DisplayName("adding mongo container with database name should set it")
    void test_8() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            var dbName = "someDbName";

            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.getDatabaseName().set(dbName));

            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            assertThat(requestedContainer.getDatabaseName().get()).isEqualTo(dbName);
        });
    }

    @Test
    @DisplayName("adding mongo container with image should set it")
    void test_9() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            var img = "someImage";

            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.getImage().set(img));

            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            assertThat(requestedContainer.getImage().get()).isEqualTo(img);
        });
    }

    @Test
    @DisplayName("adding mongo container with fixed port should set it")
    void test_10() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            var port = 42;

            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.getFixedPort().set(port));

            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            assertThat(requestedContainer.getFixedPort().get()).isEqualTo(port);
        });
    }

    @Test
    @DisplayName("if no containers added, then extension-requested container list is empty")
    void test_11() {
        runProjectFixture(fixture -> {
            var project = fixture.project();

            project.getPlugins().apply(HuskitContainersPlugin.class);

            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When containers task set to start before test (string) task, then should add 'dependsOn' to test task")
    void test_12() {
        runSingleProjectContainerFixture(fixture -> {
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            evaluateProject(project);
            gradleAssert(fixture.testTaskProvider())
                    .dependsOn(fixture.containersTask())
                    .mustRunAfter(fixture.containersTask())
                    .requiresService(fixture.dockerBuildServiceProvider());
        });
    }

    @Test
    @DisplayName("When containers task set to start before test (Task) task, then should add 'dependsOn' to test task")
    void test_13() {
        runSingleProjectContainerFixture(fixture -> {
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(project.getTasks().getByName(JavaPlugin.TEST_TASK_NAME)));
            evaluateProject(project);
            gradleAssert(fixture.testTaskProvider())
                    .dependsOn(fixture.containersTask())
                    .mustRunAfter(fixture.containersTask())
                    .requiresService(fixture.dockerBuildServiceProvider());
        });
    }

    @Test
    @DisplayName("When containers task set to start before test (TaskProvider) task, then should add 'dependsOn' to test task")
    void test_14() {
        runSingleProjectContainerFixture(fixture -> {
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(project.getTasks().named(JavaPlugin.TEST_TASK_NAME)));
            evaluateProject(project);
            gradleAssert(fixture.testTaskProvider())
                    .dependsOn(fixture.containersTask())
                    .mustRunAfter(fixture.containersTask())
                    .requiresService(fixture.dockerBuildServiceProvider());
        });
    }

    @Test
    @DisplayName("When 'start before' is not configured, containers task is not created")
    void test_15() {
        runSingleProjectContainerFixture(fixture -> {
            var project = fixture.project();
            evaluateProject(project);

            gradleAssert(project).doesNotHaveTask(ContainersTask.nameForTask(JavaPlugin.TEST_TASK_NAME));
            verifyNoInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When 'start before' is configured but no containers added, containers task doesn't start any containers")
    void test_16() {
        runSingleProjectContainerFixture(fixture -> {
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            evaluateProject(project);
            assertThat(fixture.containersTask().get().startAndReturnContainers()).isEmpty();
        });
    }

    @Test
    @DisplayName("When empty mongo spec provided, use default values")
    void test_17() {
        runSingleProjectContainerFixture(fixture -> {
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            var actual = fixture.containersTask().get().startAndReturnContainers();
            assertThat(actual).isNotEmpty();
            verifyNoInteractions(fixture.testContainersDelegateMock());
        });
    }

    protected AddContainersEnvironment findTaskAction(Task task, Class<AddContainersEnvironment> type) {
        var aTask = (DefaultTask) task;
        return aTask.getTaskActions().stream()
                .map(actionWrapper -> {
                    try {
                        for (var field : actionWrapper.getClass().getDeclaredFields()) {
                            if (field.getName().equals("action")) {
                                field.setAccessible(true);
                                var action = field.get(actionWrapper);
                                if (action.getClass().equals(type)) {
                                    return (AddContainersEnvironment) field.get(actionWrapper);
                                }
                            }
                        }
                        return null;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(String.format("No action of type [%s] found in task [%s]", type, task.getName())));
    }

    @SuppressWarnings("unchecked")
    private void runSingleProjectContainerFixture(ThrowingConsumer<SingleProjectContainerFixture> fixtureConsumer) {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            var testContainersDelegateMock = mock(
                    TestContainersDelegate.class,
                    withSettings().serializable().strictness(Strictness.STRICT_STUBS)
            );
            var testContainersDelegateExtension = new TestContainersDelegateExtension(testContainersDelegateMock);
            project.getExtensions().add(TestContainersDelegateExtension.name(), testContainersDelegateExtension);
            project.getPlugins().apply(JavaPlugin.class);
            project.getPlugins().apply(HuskitContainersPlugin.class);
            var projectBuildServices = new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
            assertThat(projectBuildServices).hasSize(1);
            var dockerBuildServiceProvider = projectBuildServices.get(0).getService();
            var dockerBuildService = (ContainersBuildService) dockerBuildServiceProvider.get();
            var maxParallelUsages = projectBuildServices.get(0).getMaxParallelUsages();
            var dockerBuildServiceParams = dockerBuildService.getParameters();
            fixtureConsumer.accept(
                    new SingleProjectContainerFixture(
                            project,
                            fixture.projectDir(),
                            fixture.objects(),
                            fixture.providers(),
                            dockerBuildService,
                            (Provider<ContainersBuildService>) dockerBuildServiceProvider,
                            maxParallelUsages,
                            dockerBuildServiceParams,
                            testContainersDelegateMock,
                            (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class)
                    )
            );
        });
    }

    private void runMultiProjectContainerFixture(ThrowingConsumer<MultiProjectContainerFixture> fixtureConsumer) {
        runProjectWithParentFixture(delegateFixture -> {
            delegateFixture.project().getPlugins().apply(HuskitContainersPlugin.class);
            delegateFixture.parentProject().getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(delegateFixture.parentProject());
            evaluateProject(delegateFixture.project());
            var projectBuildServices = new ArrayList<>(delegateFixture.project().getGradle().getSharedServices().getRegistrations());
            assertThat(projectBuildServices).hasSize(1);
            var service = projectBuildServices.get(0).getService();
            var dockerBuildService = (ContainersBuildService) service.get();
            var maxParallelUsages = projectBuildServices.get(0).getMaxParallelUsages();
            var dockerBuildServiceParams = dockerBuildService.getParameters();
            try {
                fixtureConsumer.accept(
                        new MultiProjectContainerFixture(
                                delegateFixture.project(),
                                delegateFixture.parentProject(),
                                delegateFixture.projectDir(),
                                delegateFixture.parentProjectDir(),
                                delegateFixture.objects(),
                                delegateFixture.providers(),
                                maxParallelUsages,
                                dockerBuildService,
                                dockerBuildServiceParams
                        ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Getter
    @RequiredArgsConstructor
    private static class MultiProjectContainerFixture {


        Project project;
        Project parentProject;
        File projectDir;
        File parentProjectDir;
        ObjectFactory objects;
        ProviderFactory providers;
        Provider<Integer> maxParallelUsages;
        ContainersBuildService dockerBuildService;
        ContainersBuildServiceParams dockerBuildServiceParams;
    }

    @Getter
    @RequiredArgsConstructor
    private static class SingleProjectContainerFixture {

        Project project;
        File projectDir;
        ObjectFactory objects;
        ProviderFactory providers;
        ContainersBuildService dockerBuildService;
        Provider<ContainersBuildService> dockerBuildServiceProvider;
        Provider<Integer> maxParallelUsages;
        ContainersBuildServiceParams dockerBuildServiceParams;
        TestContainersDelegate testContainersDelegateMock;
        ContainersExtension containersExtension;

        TaskProvider<org.gradle.api.tasks.testing.Test> testTaskProvider() {
            return project.getTasks().named(JavaPlugin.TEST_TASK_NAME, org.gradle.api.tasks.testing.Test.class);
        }

        TaskProvider<ContainersTask> containersTask() {
            return project.getTasks().named(ContainersTask.nameForTask(JavaPlugin.TEST_TASK_NAME), ContainersTask.class);
        }
    }
}
