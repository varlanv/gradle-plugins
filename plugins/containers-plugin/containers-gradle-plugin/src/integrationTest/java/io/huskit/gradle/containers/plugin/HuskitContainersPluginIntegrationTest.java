package io.huskit.gradle.containers.plugin;

import io.huskit.containers.model.Constants;
import io.huskit.containers.model.exception.NonUniqueContainerException;
import io.huskit.containers.testcontainers.mongo.TestContainersDelegate;
import io.huskit.gradle.commontest.GradleIntegrationTest;
import io.huskit.gradle.containers.plugin.api.CleanupSpecView;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.internal.AddContainersEnvironment;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.gradle.containers.plugin.internal.ContainersTask;
import io.huskit.gradle.containers.plugin.internal.HuskitContainersExtension;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import io.huskit.gradle.containers.plugin.internal.spec.mongo.MongoContainerRequestSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.services.BuildServiceRegistration;
import org.gradle.api.tasks.TaskProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.quality.Strictness;
import org.testcontainers.containers.MongoDBContainer;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class HuskitContainersPluginIntegrationTest implements GradleIntegrationTest {

    @Test
    @DisplayName("should create build service")
    void test_0() {
        runProjectFixture(fixture -> {
            var project = fixture.project();
            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);

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
            evaluateProject(fixture.parentProject());
            evaluateProject(fixture.project());

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
            evaluateProject(project);

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

            assertThat(project.getExtensions().findByName(HuskitContainersExtension.name())).isNotNull();
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
            containersExtension.mongo(mongo -> mongo.databaseName(dbName));

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
            containersExtension.mongo(mongo -> mongo.image(img));

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
            var hostPort = 421;

            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.port(portSpec -> portSpec.fixed(fixedPortSpec -> {
                fixedPortSpec.containerValue(Constants.Mongo.DEFAULT_PORT);
                fixedPortSpec.hostValue(hostPort);
            })));

            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            var portSpec = requestedContainer.getPort().get();
            var fixedPortSpec = portSpec.getFixed().get();
            assertThat(fixedPortSpec.getHostValue().get()).isEqualTo(hostPort);
            assertThat(fixedPortSpec.getContainerValue().get()).isEqualTo(Constants.Mongo.DEFAULT_PORT);
            assertThat(fixedPortSpec.getHostRange().getOrNull()).isNull();
            assertThat(portSpec.getDynamic().getOrNull()).isFalse();
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
    @DisplayName("When empty mongo spec provided, use default values to start mongo container")
    void test_17() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            var connectionString = "anyConnectionString";
            var port = 42;
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );

            // AND
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo spec provided with non-default exposed environment values then should use them")
    void test_18() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            var port = 42;
            var exposedPortEnv = "anyExposedPortEnv";
            var exposedDbNameEnv = "anyExposedDbNameEnv";
            var exposedConnectionStringEnv = "anyExposedConnectionStringEnv";
            var connectionString = "anyConnectionString";
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
                mongoSpec.exposedEnvironment(exposedEnvSpec -> {
                    exposedEnvSpec.connectionString(exposedConnectionStringEnv);
                    exposedEnvSpec.databaseName(exposedDbNameEnv);
                    exposedEnvSpec.port(exposedPortEnv);
                });
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyEntriesOf(
                    Map.of(
                            exposedConnectionStringEnv, connectionString,
                            exposedPortEnv, String.valueOf(port),
                            exposedDbNameEnv, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );

            // AND
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo spec provided with `newDatabaseForEachTask`, then should reuse container")
    void test_19() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            var port = 42;
            var connectionString = "anyConnectionString";
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoContainerRequestSpec ->
                    mongoContainerRequestSpec.reuse(reuseSpec ->
                            reuseSpec.newDatabaseForEachTask(true)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyInAnyOrderEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString + "/" + Constants.Mongo.DEFAULT_DB_NAME + "_1",
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME + "_1"
                    )
            );

            // AND
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo spec provided with `reuseBetweenBuilds`, calling `stop` should clear all databases except default ones instead of stopping the container")
    void test_20() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            var port = 42;
            var connectionString = "anyConnectionString";
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoContainerRequestSpec ->
                    mongoContainerRequestSpec.reuse(reuseSpec ->
                            reuseSpec.reuseBetweenBuilds(true)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );

            // AND
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).execInContainer(any(), eq("/bin/sh"), eq("-c"), eq(Constants.Mongo.DROP_COMMAND));
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo spec is not reusable, calling `stop` should stop the container")
    void test_21() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            var port = 42;
            var connectionString = "anyConnectionString";
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());

            // AND
            assertThat(taskEnvironment).containsExactlyInAnyOrderEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo reuse spec is set to never expire - should not try to stop the container")
    void test_22() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(CleanupSpecView::never)));
            var connectionString = "anyConnectionString";
            var port = 42;
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // AND
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).isNotEmpty();
            assertThat(taskEnvironment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );

            // AND
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo container throws exception on `stop` attempt - should not fail build")
    void test_23() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            var port = 42;
            var connectionString = "anyConnectionString";
            var containerStopException = new RuntimeException("anyException");
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);
            doAnswer(invocation -> {
                throw containerStopException;
            }).when(fixture.testContainersDelegateMock()).stop(any());

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());

            // AND
            assertThat(taskEnvironment).containsExactlyInAnyOrderEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo spec has two duplicate containers, then should throw exception")
    void test_24() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });

            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec -> {
            })).isInstanceOf(NonUniqueContainerException.class);
        });
    }

    @Test
    @DisplayName("When mongo container depends on non-test task, then should not expose environment")
    void test_25() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            var port = 42;
            var connectionString = "anyConnectionString";
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(BasePlugin.CLEAN_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask(BasePlugin.CLEAN_TASK_NAME).get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction(BasePlugin.CLEAN_TASK_NAME);
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(project.getTasks().getByName(BasePlugin.CLEAN_TASK_NAME), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());

            // AND
            assertThat(taskEnvironment).isEmpty();
        });
    }

    @Test
    @DisplayName("mongo container when `containerValue` is not set, then should use default mongo port")
    void test_26() {
        runSingleProjectContainerFixture(fixture -> {
            var hostPort = 421;

            fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostValue(hostPort))));

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            var portSpec = requestedContainer.getPort().get();
            var fixedPortSpec = portSpec.getFixed().get();
            assertThat(fixedPortSpec.getHostValue().get()).isEqualTo(hostPort);
            assertThat(fixedPortSpec.getContainerValue().get()).isEqualTo(Constants.Mongo.DEFAULT_PORT);
            assertThat(fixedPortSpec.getHostRange().getOrNull()).isNull();
            assertThat(portSpec.getDynamic().getOrNull()).isFalse();
        });
    }

    @Test
    @DisplayName("mongo container when `fixed` port is not set, then use dynamic port")
    void test_27() {
        runSingleProjectContainerFixture(fixture -> {
            fixture.containersExtension().mongo(mongo -> {
            });

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            var portSpec = requestedContainer.getPort().get();
            var fixedPortSpec = portSpec.getFixed().get();
            assertThat(fixedPortSpec.getHostValue().getOrNull()).isNull();
            assertThat(fixedPortSpec.getContainerValue().getOrNull()).isNull();
            assertThat(fixedPortSpec.getHostRange().getOrNull()).isNull();
            assertThat(portSpec.getDynamic().getOrNull()).isTrue();
        });
    }

    @Test
    @DisplayName("mongo container when `fixed` port spec is empty, then should throw exception")
    void test_28() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fixed port must be set to");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("mongo container when `fixed` port is already set to `hostRange` and trying to set `hostValue`, then should throw exception")
    void test_29() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                                fixedPortSpec.hostRange(1, 2);
                                fixedPortSpec.hostValue(42);
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("42")
                    .hasMessageContaining("[1, 2]")
                    .hasMessageContaining("Can't set port");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("mongo container when `fixed` port is already set to `hostValue` and trying to set `hostRange`, then should throw exception")
    void test_30() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                                fixedPortSpec.hostValue(42);
                                fixedPortSpec.hostRange(1, 2);
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("42")
                    .hasMessageContaining("[1, 2]")
                    .hasMessageContaining("Can't set port");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -42})
    @DisplayName("mongo container when `fixed` port is set to negative `hostValue`, should throw exception")
    void test_31(Integer hostValue) {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                                fixedPortSpec.hostValue(hostValue);
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(hostValue.toString())
                    .hasMessageContaining("must be greater than 0");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -42})
    @DisplayName("mongo container when `fixed` port is set to negative `containerValue`, should throw exception")
    void test_32(Integer containerValue) {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                                fixedPortSpec.hostValue(1);
                                fixedPortSpec.containerValue(containerValue);
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(containerValue.toString())
                    .hasMessageContaining("must be greater than 0");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @CsvSource({
            "0, 1",
            "1, 0",
            "0, 0",
            "-1, 1",
            "1, -1",
            "-1, -1"
    })
    @DisplayName("mongo container when `fixed` port is set to negative `hostPortFrom` or `hostPortTo`, should throw exception")
    void test_33(Integer hostPortFrom, Integer hostPortTo) {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostRange(hostPortFrom, hostPortTo)))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fixed port range %s", List.of(hostPortFrom, hostPortTo))
                    .hasMessageContaining("must be greater than 0");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    @DisplayName("mongo container when `fixed` port range `hostPortFrom` is equal to or greater than `hostPortTo`, should throw exception")
    void test_34(Integer hostPortFrom) {
        runSingleProjectContainerFixture(fixture -> {
            var hostPortTo = 5;
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostRange(hostPortFrom, hostPortTo)))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be less than range end [%s]", hostPortTo);

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` time unit is unknown, then should throw exception")
    void test_35() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(1, "sec")))))
                    .hasMessageContaining("Unavailable unit value - 'sec'");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` when known time unit is provided, should configure cleanup")
    void test_36() {
        runSingleProjectContainerFixture(fixture -> {
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(1, "hours"))));

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            var cleanupSpec = reuseSpec.getCleanupSpec().get();
            assertThat(cleanupSpec.getCleanupAfter().get()).isEqualTo(Duration.ofHours(1));
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` time unit is not allowed, then should throw exception")
    void test_37() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(1, ChronoUnit.DECADES)))))
                    .hasMessageContaining("Unavailable unit value - '%s'", ChronoUnit.DECADES);

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` negative time value is provided, then should throw exception")
    void test_38() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(-1, ChronoUnit.DAYS)))))
                    .hasMessageContaining("`cleanupAfter` [%s] cannot be negative", Duration.ofDays(-1));

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` time value less than 60 seconds is provided, then should throw exception")
    void test_39() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(59, ChronoUnit.SECONDS)))))
                    .hasMessageContaining("cannot be less than 60 seconds");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` 0 time value is provided, should consider it as 'forever'")
    void test_40() {
        runSingleProjectContainerFixture(fixture -> {
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(0, ChronoUnit.SECONDS))));

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            var cleanupSpec = reuseSpec.getCleanupSpec().get();
            assertThat(cleanupSpec.getCleanupAfter().get()).isEqualTo(Duration.ZERO);
        });
    }

    @Test
    @DisplayName("when mongo `cleanupSpec` Duration object is provided, then sould convert it to millis")
    void test_41() {
        runSingleProjectContainerFixture(fixture -> {
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(Duration.ofSeconds(100)))));

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            var cleanupSpec = reuseSpec.getCleanupSpec().get();
            assertThat(cleanupSpec.getCleanupAfter().get()).isEqualTo(Duration.ofSeconds(100));
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @DisplayName("when mongo `reuseSpec` enabled value is provided, then should configure reuse")
    void test_42(Boolean reuseEnabled) {
        runSingleProjectContainerFixture(fixture -> {
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec -> reuseSpec.enabled(reuseEnabled)));

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            assertThat(reuseSpec.getEnabled().get()).isEqualTo(reuseEnabled);
        });
    }

    @Test
    @DisplayName("when mongo `shouldStartBefore` spec is already set and trying to set it again, then should throw exception")
    void test_43() {
        runSingleProjectContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.containersExtension().shouldStartBefore(shouldStartBeforeSpec -> {
                shouldStartBeforeSpec.task(BasePlugin.CLEAN_TASK_NAME);
                shouldStartBeforeSpec.task(JavaPlugin.TEST_TASK_NAME);
            })).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already been set")
                    .hasMessageContaining("shouldRunBefore");

            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When mongo port is set to fixed `hostValue`, then should start mongo container with specified port")
    void test_44() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var port = 555;
            var connectionString = "anyConnectionString";
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostValue(port))));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(connectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(port);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // AND
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).isNotEmpty();
            assertThat(taskEnvironment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, connectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(port),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );

            // AND
            ArgumentCaptor<MongoDBContainer> mongoContainerCaptor = ArgumentCaptor.captor();
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(mongoContainerCaptor.capture());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());

            // AND
            var mongoDBContainer = mongoContainerCaptor.getValue();
            assertThat(mongoDBContainer.getDockerImageName()).isEqualTo(Constants.Mongo.DEFAULT_IMAGE);
            assertThat(mongoDBContainer.getExposedPorts()).containsExactly(Constants.Mongo.DEFAULT_PORT);
        });
    }

    protected static AddContainersEnvironment findTaskAction(Task task, Class<AddContainersEnvironment> type) {
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
            project.getPlugins().apply(JavaPlugin.class);
            project.getPlugins().apply(HuskitContainersPlugin.class);
            Supplier<ArrayList<BuildServiceRegistration<?, ?>>> buildServiceRegistrations = () -> new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
            Supplier<Provider<ContainersBuildService>> dockerBuildServiceProvider = () -> (Provider<ContainersBuildService>) buildServiceRegistrations.get().get(0).getService();
            Supplier<ContainersBuildService> dockerBuildService = () -> dockerBuildServiceProvider.get().get();
            Supplier<Provider<Integer>> maxParallelUsages = () -> buildServiceRegistrations.get().get(0).getMaxParallelUsages();
            Supplier<ContainersBuildServiceParams> dockerBuildServiceParams = () -> dockerBuildService.get().getParameters();

            fixtureConsumer.accept(
                    new SingleProjectContainerFixture(
                            project,
                            fixture.projectDir(),
                            fixture.objects(),
                            fixture.providers(),
                            dockerBuildService,
                            dockerBuildServiceProvider,
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
        Supplier<ContainersBuildService> dockerBuildService;
        Supplier<Provider<ContainersBuildService>> dockerBuildServiceProvider;
        Supplier<Provider<Integer>> maxParallelUsages;
        Supplier<ContainersBuildServiceParams> dockerBuildServiceParams;
        TestContainersDelegate testContainersDelegateMock;
        HuskitContainersExtension containersExtension;

        ContainersBuildService dockerBuildService() {
            return dockerBuildService.get();
        }

        Provider<ContainersBuildService> dockerBuildServiceProvider() {
            return dockerBuildServiceProvider.get();
        }

        Provider<Integer> maxParallelUsages() {
            return maxParallelUsages.get();
        }

        TaskProvider<org.gradle.api.tasks.testing.Test> testTaskProvider() {
            return project.getTasks().named(JavaPlugin.TEST_TASK_NAME, org.gradle.api.tasks.testing.Test.class);
        }

        TaskProvider<ContainersTask> containersTask() {
            return containersTask(JavaPlugin.TEST_TASK_NAME);
        }

        TaskProvider<ContainersTask> containersTask(String dependentTaskName) {
            return project.getTasks().named(ContainersTask.nameForTask(dependentTaskName), ContainersTask.class);
        }

        AddContainersEnvironment getAddContainersEnvironmentAction() {
            return findTaskAction(testTaskProvider().get(), AddContainersEnvironment.class);
        }

        AddContainersEnvironment getAddContainersEnvironmentAction(TaskProvider<DefaultTask> taskTaskProvider) {
            return findTaskAction(taskTaskProvider.get(), AddContainersEnvironment.class);
        }

        AddContainersEnvironment getAddContainersEnvironmentAction(String taskName) {
            return findTaskAction(project.getTasks().named(taskName).get(), AddContainersEnvironment.class);
        }
    }
}
