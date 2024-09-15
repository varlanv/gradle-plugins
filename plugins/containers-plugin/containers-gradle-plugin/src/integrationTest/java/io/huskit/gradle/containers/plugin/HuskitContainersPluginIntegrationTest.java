package io.huskit.gradle.containers.plugin;

import io.huskit.common.Sneaky;
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

    Integer anyFixedPort = 42;
    Integer anyHostPort = 421;
    String anyConnectionString = "anyConnectionString";
    String anyDbName = "anyDbName";
    String anyImage = "anyImage";
    String anyExposedPortEnv = "anyExposedPortEnv";
    String anyExposedDbNameEnv = "anyExposedDbNameEnv";
    String anyExposedConnectionStringEnv = "anyExposedConnectionStringEnv";

    @Test
    @DisplayName("Should create build service")
    void should_create_build_service() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);

            // THEN
            gradleAssert(project).hasOnlyOneService(ContainersBuildService.name());
        });
    }

    @Test
    @DisplayName("If build service already added and plugin applied, then no error thrown")
    void if_build_service_already_added_and_plugin_applied_then_no_error_thrown() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            project.getGradle().getSharedServices().registerIfAbsent(
                    ContainersBuildService.name(),
                    ContainersBuildService.class,
                    params -> {
                    }
            );

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);

            // THEN
            gradleAssert(project).hasOnlyOneService(ContainersBuildService.name());
        });
    }

    @Test
    @DisplayName("If parent project already created build service and plugin applied, then no error thrown")
    void if_parent_project_already_created_build_service_and_plugin_applied_then_no_error_thrown() {
        runProjectWithParentFixture(fixture -> {
            // WHEN
            fixture.parentProject().getPlugins().apply(HuskitContainersPlugin.class);
            fixture.project().getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(fixture.parentProject());
            evaluateProject(fixture.project());

            // THEN
            gradleAssert(fixture.project()).hasOnlyOneService(ContainersBuildService.name());
            gradleAssert(fixture.parentProject()).hasOnlyOneService(ContainersBuildService.name());
        });
    }

    @Test
    @DisplayName("When build service evaluated, no exception thrown")
    void when_build_service_evaluated_no_exception_thrown() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);

            // WHEN
            var buildServiceProvider = List.copyOf(
                    project.getGradle().getSharedServices().getRegistrations()
            ).get(0).getService();

            // THEN
            assertThat(buildServiceProvider.isPresent()).isTrue();
        });
    }

    @Test
    @DisplayName("If there is one project with plugin, then max parallel uses should be 1")
    void if_there_is_one_project_with_plugin_then_max_parallel_uses_should_be_1() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);

            // THEN
            gradleAssert(project).withBuildServiceRegistration(ContainersBuildService.name(), registration -> {
                assertThat(registration.getMaxParallelUsages().isPresent()).isTrue();
                assertThat(registration.getMaxParallelUsages().get()).isEqualTo(1);
            });
        });
    }

    @Test
    @DisplayName("If there are two projects with plugin, then max parallel uses should be 2")
    void if_there_are_two_projects_with_plugin_then_max_parallel_uses_should_be_2() {
        runMultiProjectContainerFixture(fixture -> {
            // EXPECT
            assertThat(fixture.maxParallelUsages.get()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("When applying plugin, then should create extension")
    void when_applying_plugin_then_should_create_extension() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);
            evaluateProject(project);

            // THEN
            gradleAssert(project)
                    .hasExtensionWithName(HuskitContainersExtension.name())
                    .hasExtensionWithType(ContainersExtension.class);
        });
    }

    @Test
    @DisplayName("Adding mongo container with database name should set it")
    void adding_mongo_container_with_database_name_should_set_it() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.databaseName(anyDbName));

            // THEN
            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            assertThat(requestedContainer.getDatabaseName().get()).isEqualTo(anyDbName);
        });
    }

    @Test
    @DisplayName("Adding mongo container with image should set it")
    void adding_mongo_container_with_image_should_set_it() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.image(anyImage));

            // THEN
            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            assertThat(requestedContainer.getImage().get()).isEqualTo(anyImage);
        });
    }

    @Test
    @DisplayName("Adding mongo container with fixed port should set it")
    void adding_mongo_container_with_fixed_port_should_set_it() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            containersExtension.mongo(mongo -> mongo.port(portSpec -> portSpec.fixed(fixedPortSpec -> {
                fixedPortSpec.containerValue(Constants.Mongo.DEFAULT_PORT);
                fixedPortSpec.hostValue(anyHostPort);
            })));

            // THEN
            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            var portSpec = requestedContainer.getPort().get();
            var fixedPortSpec = portSpec.getFixed().get();
            assertThat(fixedPortSpec.getHostValue().get()).isEqualTo(anyHostPort);
            assertThat(fixedPortSpec.getContainerValue().get()).isEqualTo(Constants.Mongo.DEFAULT_PORT);
            assertThat(fixedPortSpec.getHostRange().isPresent()).isFalse();
            assertThat(portSpec.getDynamic().get()).isFalse();
        });
    }

    @Test
    @DisplayName("If no containers added, then extension-requested container list is empty")
    void if_no_containers_added_then_extension_requested_container_list_is_empty() {
        runProjectFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            project.getPlugins().apply(HuskitContainersPlugin.class);

            // THEN
            var containersExtension = (HuskitContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
            var requestedContainers = containersExtension.getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When containers task set to start before test (string) task, then should add 'dependsOn' to test task")
    void when_containers_task_set_to_start_before_test_string_task_then_should_add_depends_on_to_test_task() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            evaluateProject(project);

            // THEN
            gradleAssert(fixture.testTaskProvider())
                    .dependsOn(fixture.containersTask())
                    .mustRunAfter(fixture.containersTask())
                    .requiresService(fixture.dockerBuildServiceProvider());
        });
    }

    @Test
    @DisplayName("When containers task set to start before test (Task) task, then should add 'dependsOn' to test task")
    void when_containers_task_set_to_start_before_test_task_then_should_add_depends_on_to_test_task() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(project.getTasks().getByName(JavaPlugin.TEST_TASK_NAME)));
            evaluateProject(project);

            // THEN
            gradleAssert(fixture.testTaskProvider())
                    .dependsOn(fixture.containersTask())
                    .mustRunAfter(fixture.containersTask())
                    .requiresService(fixture.dockerBuildServiceProvider());
        });
    }

    @Test
    @DisplayName("When containers task set to start before test (TaskProvider) task, then should add 'dependsOn' to test task")
    void when_containers_task_set_to_start_before_test_task_provider_task_then_should_add_depends_on_to_test_task() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(project.getTasks().named(JavaPlugin.TEST_TASK_NAME)));
            evaluateProject(project);

            // THEN
            gradleAssert(fixture.testTaskProvider())
                    .dependsOn(fixture.containersTask())
                    .mustRunAfter(fixture.containersTask())
                    .requiresService(fixture.dockerBuildServiceProvider());
        });
    }

    @Test
    @DisplayName("When 'start before' is not configured, containers task is not created")
    void when_start_before_is_not_configured_containers_task_is_not_created() {
        runSingleProjectContainerFixture(fixture -> {
            // WHEN
            var project = fixture.project();
            evaluateProject(project);

            // THEN
            gradleAssert(project).doesNotHaveTask(ContainersTask.nameForTask(JavaPlugin.TEST_TASK_NAME));
            verifyNoInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When 'start before' is configured but no containers added, containers task doesn't start any containers")
    void when_start_before_is_configured_but_no_containers_added_containers_task_doesnt_start_any_containers() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();

            // WHEN
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            evaluateProject(project);

            // THEN
            assertThat(fixture.containersTask().get().startAndReturnContainers()).isEmpty();
        });
    }

    @Test
    @DisplayName("When empty mongo spec provided, use default values to start mongo container")
    void when_empty_mongo_spec_provided_use_default_values_to_start_mongo_container() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When empty mongo spec provided, should call testContainersDelegate methods")
    void when_empty_mongo_spec_provided_should_call_testContainersDelegate_methods() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // THEN
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
    void when_mongo_spec_provided_with_non_default_exposed_environment_values_then_should_use_them() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
                mongoSpec.exposedEnvironment(exposedEnvSpec -> {
                    exposedEnvSpec.connectionString(anyExposedConnectionStringEnv);
                    exposedEnvSpec.databaseName(anyExposedDbNameEnv);
                    exposedEnvSpec.port(anyExposedPortEnv);
                });
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyEntriesOf(
                    Map.of(
                            anyExposedConnectionStringEnv, anyConnectionString,
                            anyExposedPortEnv, String.valueOf(anyFixedPort),
                            anyExposedDbNameEnv, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo spec provided with non-default exposed environment values then should call testContainersDelegate methods")
    void when_mongo_spec_provided_with_non_default_exposed_environment_values_then_should_call_testContainersDelegate_methods() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
                mongoSpec.exposedEnvironment(exposedEnvSpec -> {
                    exposedEnvSpec.connectionString(anyExposedConnectionStringEnv);
                    exposedEnvSpec.databaseName(anyExposedDbNameEnv);
                    exposedEnvSpec.port(anyExposedPortEnv);
                });
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // THEN
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
    void when_mongo_spec_provided_with_new_database_for_each_task_then_should_reuse_container() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoContainerRequestSpec ->
                    mongoContainerRequestSpec.reuse(reuseSpec ->
                            reuseSpec.newDatabaseForEachTask(true)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyInAnyOrderEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString + "/" + Constants.Mongo.DEFAULT_DB_NAME + "_1",
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME + "_1"
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo spec provided with `newDatabaseForEachTask`, then should call testContainersDelegate methods")
    void when_mongo_spec_provided_with_new_database_for_each_task_then_should_call_testContainersDelegate_methods() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoContainerRequestSpec ->
                    mongoContainerRequestSpec.reuse(reuseSpec ->
                            reuseSpec.newDatabaseForEachTask(true)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // THEN
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo spec has `reuseBetweenBuilds`, calling `stop` "
            + "should clear all databases except default ones instead of stopping the container")
    void when_mongo_spec_provided_with_reuse_between_builds_calling_stop_should_clear_all_databases() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoContainerRequestSpec ->
                    mongoContainerRequestSpec.reuse(reuseSpec ->
                            reuseSpec.reuseBetweenBuilds(true)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
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
    @DisplayName("When mongo spec provided with `reuseBetweenBuilds`, calling `stop` should set correct environment")
    void when_mongo_spec_provided_with_reuse_between_builds_calling_stop_should_set_correct_environment() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoContainerRequestSpec ->
                    mongoContainerRequestSpec.reuse(reuseSpec ->
                            reuseSpec.reuseBetweenBuilds(true)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var environment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            assertThat(environment).isNotEmpty();
            assertThat(environment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo spec is not reusable, calling `stop` should stop the container")
    void when_mongo_spec_is_not_reusable_calling_stop_should_stop_the_container() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo spec is not reusable, calling `stop` then should set correct environment")
    void when_mongo_spec_is_not_reusable_calling_stop_then_should_set_correct_environment() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).containsExactlyInAnyOrderEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo reuse spec is set to never expire - should not try to stop the container")
    void when_mongo_reuse_spec_is_set_to_never_expire_should_not_try_to_stop_the_container() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(CleanupSpecView::never)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // AND
            fixture.dockerBuildService().close();

            // THEN
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo reuse spec is set to never expire - should set correct environment")
    void when_mongo_reuse_spec_is_set_to_never_expire_should_set_correct_environment() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(CleanupSpecView::never)));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());

            // AND
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).isNotEmpty();
            assertThat(taskEnvironment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo container throws exception on `stop` attempt - should not fail build")
    void when_mongo_container_throws_exception_on_stop_attempt_should_not_fail_build() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);
            var containerStopException = new RuntimeException("anyException");
            doAnswer(invocation -> {
                throw containerStopException;
            }).when(fixture.testContainersDelegateMock()).stop(any());

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());
        });
    }

    @Test
    @DisplayName("When mongo container throws exception on `stop` then should set correct environment")
    void when_mongo_container_throws_exception_on_stop_then_should_set_correct_environment() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);
            var containerStopException = new RuntimeException("anyException");
            doAnswer(invocation -> {
                throw containerStopException;
            }).when(fixture.testContainersDelegateMock()).stop(any());

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).containsExactlyInAnyOrderEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    @Test
    @DisplayName("When mongo spec has two duplicate containers, then should throw exception")
    void when_mongo_spec_has_two_duplicate_containers_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // WHEN
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });

            // THEN
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec -> {
            })).isInstanceOf(NonUniqueContainerException.class);
        });
    }

    @Test
    @DisplayName("When mongo container depends on non-test task, then should not expose environment")
    void when_mongo_container_depends_on_non_test_task_then_should_not_expose_environment() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(BasePlugin.CLEAN_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec -> {
            });
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask(BasePlugin.CLEAN_TASK_NAME).get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction(BasePlugin.CLEAN_TASK_NAME);
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(
                    project.getTasks().getByName(BasePlugin.CLEAN_TASK_NAME),
                    fixture.testContainersDelegateMock());
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).isEmpty();
            // AND
            verify(fixture.testContainersDelegateMock()).start(any());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());

        });
    }

    @Test
    @DisplayName("Mongo container when `containerValue` is not set, then should use default mongo port")
    void mongo_container_when_containerValue_is_not_set_then_should_use_default_mongo_port() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostValue(anyHostPort))));

            // EXPECT
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            var portSpec = requestedContainer.getPort().get();
            var fixedPortSpec = portSpec.getFixed().get();
            assertThat(fixedPortSpec.getHostValue().get()).isEqualTo(anyHostPort);
            assertThat(fixedPortSpec.getContainerValue().get()).isEqualTo(Constants.Mongo.DEFAULT_PORT);
            assertThat(fixedPortSpec.getHostRange().isPresent()).isFalse();
            assertThat(portSpec.getDynamic().getOrNull()).isFalse();
        });
    }

    @Test
    @DisplayName("Mongo container when `fixed` port is not set, then use dynamic port")
    void mongo_container_when_fixed_port_is_not_set_then_use_dynamic_port() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            fixture.containersExtension().mongo(mongo -> {
            });

            // EXPECT
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var requestedContainer = (MongoContainerRequestSpec) requestedContainers.get(0);
            var portSpec = requestedContainer.getPort().get();
            var fixedPortSpec = portSpec.getFixed().get();
            assertThat(fixedPortSpec.getHostValue().getOrNull()).isNull();
            assertThat(fixedPortSpec.getContainerValue().getOrNull()).isNull();
            assertThat(fixedPortSpec.getHostRange().isPresent()).isFalse();
            assertThat(portSpec.getDynamic().getOrNull()).isTrue();
        });
    }

    @Test
    @DisplayName("Mongo container when `fixed` port spec is empty, then should throw exception")
    void mongo_container_when_fixed_port_spec_is_empty_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fixed port must be set to");

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("Mongo container when already set `hostRange` and trying to set `hostValue`, then should throw exception")
    void mongo_container_when_already_set_hostRange_and_trying_to_set_hostValue_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
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

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("Mongo container when `fixed` port is already set to `hostValue` and try to set `hostRange` then should throw exception")
    void mongo_container_when_fixed_port_is_already_set_to_hostValue_and_trying_to_set_hostRange_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
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

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -42})
    @DisplayName("Mongo container when `fixed` port is set to negative `hostValue`, should throw exception")
    void mongo_container_when_fixed_port_is_set_to_negative_hostValue_then_should_throw_exception(Integer hostValue) {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                                fixedPortSpec.hostValue(hostValue);
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(hostValue.toString())
                    .hasMessageContaining("must be greater than 0");

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -42})
    @DisplayName("Mongo container when `fixed` port is set to negative `containerValue`, should throw exception")
    void mongo_container_when_fixed_port_is_set_to_negative_containerValue_then_should_throw_exception(Integer containerValue) {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec -> {
                                fixedPortSpec.hostValue(1);
                                fixedPortSpec.containerValue(containerValue);
                            }))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(containerValue.toString())
                    .hasMessageContaining("must be greater than 0");

            // AND
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
    @DisplayName("Mongo container when negative `hostPortFrom` or `hostPortTo`, should throw exception")
    void mongo_container_when_negative_hostPortFrom_or_hostPortTo_then_should_throw_exception(Integer hostPortFrom, Integer hostPortTo) {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongo ->
                    mongo.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostRange(hostPortFrom, hostPortTo)))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Fixed port range %s", List.of(hostPortFrom, hostPortTo))
                    .hasMessageContaining("must be greater than 0");

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 6})
    @DisplayName("Mongo container when `hostPortFrom` is equal to or greater than `hostPortTo`, should throw exception")
    void mongo_container_when_hostPortFrom_is_equal_to_or_greater_than_hostPortTo_then_should_throw_exception(int hostPortFrom) {
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
    @DisplayName("When mongo `cleanupSpec` time unit is unknown, then should throw exception")
    void when_mongo_cleanupSpec_time_unit_is_unknown_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(1, "sec")))))
                    .hasMessageContaining("Unavailable unit value - 'sec'");

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When mongo `cleanupSpec` when known time unit is provided, should configure cleanup")
    void when_mongo_cleanupSpec_when_known_time_unit_is_provided_should_configure_cleanup() {
        runSingleProjectContainerFixture(fixture -> {
            // WHEN
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(1, "hours"))));

            // THEN
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            var cleanupSpec = reuseSpec.getCleanupSpec().get();
            assertThat(cleanupSpec.getCleanupAfter().get()).isEqualTo(Duration.ofHours(1));
        });
    }

    @Test
    @DisplayName("When mongo `cleanupSpec` time unit is not allowed, then should throw exception")
    void when_mongo_cleanupSpec_time_unit_is_not_allowed_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(1, ChronoUnit.DECADES)))))
                    .hasMessageContaining("Unavailable unit value - '%s'", ChronoUnit.DECADES);

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When mongo `cleanupSpec` negative time value is provided, then should throw exception")
    void when_mongo_cleanupSpec_negative_time_value_is_provided_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(-1, ChronoUnit.DAYS)))))
                    .hasMessageContaining("`cleanupAfter` [%s] cannot be negative", Duration.ofDays(-1));

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When mongo `cleanupSpec` time value less than 60 seconds is provided, then should throw exception")
    void when_mongo_cleanupSpec_time_value_less_than_60_seconds_is_provided_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(59, ChronoUnit.SECONDS)))))
                    .hasMessageContaining("cannot be less than 60 seconds");

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When mongo `cleanupSpec` 0 time value is provided, should consider it as 'forever'")
    void when_mongo_cleanupSpec_0_time_value_is_provided_should_consider_it_as_forever() {
        runSingleProjectContainerFixture(fixture -> {
            // WHEN
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(0, ChronoUnit.SECONDS))));

            // THEN
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            var cleanupSpec = reuseSpec.getCleanupSpec().get();
            assertThat(cleanupSpec.getCleanupAfter().get()).isEqualTo(Duration.ZERO);
        });
    }

    @Test
    @DisplayName("When mongo `cleanupSpec` Duration object is provided, then sould convert it to millis")
    void when_mongo_cleanupSpec_duration_object_is_provided_then_should_convert_it_to_millis() {
        runSingleProjectContainerFixture(fixture -> {
            // WHEN
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec ->
                            reuseSpec.cleanup(cleanupSpec -> cleanupSpec.after(Duration.ofSeconds(100)))));

            // THEN
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
    @DisplayName("When mongo `reuseSpec` enabled value is provided, then should configure reuse")
    void when_mongo_reuseSpec_enabled_value_is_provided_then_should_configure_reuse(boolean reuseEnabled) {
        runSingleProjectContainerFixture(fixture -> {
            // WHEN
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.reuse(reuseSpec -> reuseSpec.enabled(reuseEnabled)));

            // THEN
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).hasSize(1);
            var containerSpec = (MongoContainerRequestSpec) requestedContainers.get(0);
            var reuseSpec = containerSpec.getReuse().get();
            assertThat(reuseSpec.getEnabled().get()).isEqualTo(reuseEnabled);
        });
    }

    @Test
    @DisplayName("When mongo `shouldStartBefore` spec is already set and trying to set it again, then should throw exception")
    void when_mongo_shouldStartBefore_spec_is_already_set_and_trying_to_set_it_again_then_should_throw_exception() {
        runSingleProjectContainerFixture(fixture -> {
            // EXPECT
            assertThatThrownBy(() -> fixture.containersExtension().shouldStartBefore(shouldStartBeforeSpec -> {
                shouldStartBeforeSpec.task(BasePlugin.CLEAN_TASK_NAME);
                shouldStartBeforeSpec.task(JavaPlugin.TEST_TASK_NAME);
            })).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already been set")
                    .hasMessageContaining("shouldRunBefore");

            // AND
            var requestedContainers = fixture.containersExtension().getContainersRequestedByUser().get();
            assertThat(requestedContainers).isEmpty();
        });
    }

    @Test
    @DisplayName("When mongo port is set to fixed `hostValue`, then should start mongo container with specified port")
    void when_mongo_port_is_set_to_fixed_hostValue_then_should_start_mongo_container_with_specified_port() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostValue(anyFixedPort))));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            addContainersEnvironmentAction.executeAndReturn(fixture.testTaskProvider().get(), fixture.testContainersDelegateMock());

            // AND
            fixture.dockerBuildService().close();

            // THEN
            ArgumentCaptor<MongoDBContainer> mongoContainerCaptor = ArgumentCaptor.captor();
            verify(fixture.testContainersDelegateMock()).getConnectionString(any());
            verify(fixture.testContainersDelegateMock()).start(mongoContainerCaptor.capture());
            verify(fixture.testContainersDelegateMock()).stop(any());
            verify(fixture.testContainersDelegateMock()).getFirstMappedPort(any());
            verify(fixture.testContainersDelegateMock()).setReuse();
            verify(fixture.testContainersDelegateMock()).getExistingContainer(any());
            verifyNoMoreInteractions(fixture.testContainersDelegateMock());

            // AND
            var mongoDbContainer = mongoContainerCaptor.getValue();
            assertThat(mongoDbContainer.getDockerImageName()).isEqualTo(Constants.Mongo.DEFAULT_IMAGE);
            assertThat(mongoDbContainer.getExposedPorts()).containsExactly(Constants.Mongo.DEFAULT_PORT);
        });
    }

    @Test
    @DisplayName("When mongo port is set to fixed `hostValue`, then should set correct environment")
    void when_mongo_port_is_set_to_fixed_hostValue_then_should_set_correct_environment() {
        runSingleProjectContainerFixture(fixture -> {
            // GIVEN
            var project = fixture.project();
            fixture.containersExtension().shouldStartBefore(spec -> spec.task(JavaPlugin.TEST_TASK_NAME));
            fixture.containersExtension().mongo(mongoSpec ->
                    mongoSpec.port(portSpec ->
                            portSpec.fixed(fixedPortSpec ->
                                    fixedPortSpec.hostValue(anyFixedPort))));
            evaluateProject(project);
            when(fixture.testContainersDelegateMock().getConnectionString(any())).thenReturn(anyConnectionString);
            when(fixture.testContainersDelegateMock().getFirstMappedPort(any())).thenReturn(anyFixedPort);

            // WHEN
            fixture.containersTask().get().startAndReturnContainers(fixture.testContainersDelegateMock());
            var addContainersEnvironmentAction = fixture.getAddContainersEnvironmentAction();
            var taskEnvironment = addContainersEnvironmentAction.executeAndReturn(
                    fixture.testTaskProvider().get(),
                    fixture.testContainersDelegateMock());

            // AND
            fixture.dockerBuildService().close();

            // THEN
            assertThat(taskEnvironment).isNotEmpty();
            assertThat(taskEnvironment).containsExactlyEntriesOf(
                    Map.of(
                            Constants.Mongo.DEFAULT_CONNECTION_STRING_ENV, anyConnectionString,
                            Constants.Mongo.DEFAULT_PORT_ENV, String.valueOf(anyFixedPort),
                            Constants.Mongo.DEFAULT_DB_NAME_ENV, Constants.Mongo.DEFAULT_DB_NAME
                    )
            );
        });
    }

    protected static AddContainersEnvironment findTaskAction(Task task, Class<AddContainersEnvironment> type) {
        return ((DefaultTask) task).getTaskActions().stream()
                .map(actionWrapper -> {
                    try {
                        for (var field : actionWrapper.getClass().getDeclaredFields()) {
                            if ("action".equals(field.getName())) {
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
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(
                                "No action of type [%s] found in task [%s]",
                                type, task.getName())));
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
            Supplier<List<BuildServiceRegistration<?, ?>>> buildServiceRegistrations = () ->
                    new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
            Supplier<Provider<ContainersBuildService>> dockerBuildServiceProvider = () ->
                    (Provider<ContainersBuildService>) buildServiceRegistrations.get().get(0).getService();
            Supplier<ContainersBuildService> dockerBuildService = () ->
                    dockerBuildServiceProvider.get().get();
            Supplier<Provider<Integer>> maxParallelUsages = () ->
                    buildServiceRegistrations.get().get(0).getMaxParallelUsages();
            Supplier<ContainersBuildServiceParams> dockerBuildServiceParams = () ->
                    dockerBuildService.get().getParameters();

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
                Sneaky.rethrow(e);
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
