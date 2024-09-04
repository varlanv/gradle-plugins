package io.huskit.gradle.containers.plugin;

import io.huskit.gradle.commontest.BaseGradleIntegrationTest;
import io.huskit.gradle.containers.plugin.api.ContainersExtension;
import io.huskit.gradle.containers.plugin.api.MongoContainerRequestedByUser;
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams;
import io.huskit.gradle.containers.plugin.internal.DockerContainersExtension;
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class HuskitContainersPluginIntegrationTest extends BaseGradleIntegrationTest {

    @Test
    @DisplayName("should create build service")
    void test_0() {
        var project = setupProject();

        project.getPlugins().apply(HuskitContainersPlugin.class);

        var buildServices = new ArrayList<>(project.getGradle().getSharedServices().getRegistrations());
        assertThat(buildServices).hasSize(1);
        assertThat(buildServices.get(0).getName()).isEqualTo(ContainersBuildService.name());
    }

    @Test
    @DisplayName("if build service already added, then no error thrown")
    void test_1() {
        var project = setupProject();

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
    }

    @Test
    @DisplayName("if parent project already created build service, then no error thrown")
    void test_2() {
        var fixture = setupFixtureWithParentProject();

        fixture.parentProject().getPlugins().apply(HuskitContainersPlugin.class);
        fixture.project().getPlugins().apply(HuskitContainersPlugin.class);

        var projectBuildServices = new ArrayList<>(fixture.project().getGradle().getSharedServices().getRegistrations());
        var parentProjectBuildServices = new ArrayList<>(fixture.parentProject().getGradle().getSharedServices().getRegistrations());
        assertThat(projectBuildServices).hasSize(1);
        assertThat(parentProjectBuildServices).hasSize(1);
        assertThat(projectBuildServices.get(0)).isEqualTo(parentProjectBuildServices.get(0));
        assertThat(projectBuildServices.get(0).getName()).isEqualTo(ContainersBuildService.name());
    }

    @Test
    @DisplayName("if parent project already created build service, and build service evaluated, then no error thrown")
    void test_3() {
        var fixture = setupFixtureWithParentProject();

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
    }

    @Test
    @DisplayName("when build service evaluated, no exception thrown")
    void test_4() {
        var project = setupProject();

        project.getPlugins().apply(HuskitContainersPlugin.class);
        evaluateProject(project);
        var buildService = List.copyOf(project.getGradle().getSharedServices().getRegistrations()).get(0).getService().getOrNull();

        assertThat(buildService).isNotNull();
    }

    @Test
    @DisplayName("if there is one project with plugin, then max parallel uses should be 1")
    void test_5() {
        var project = setupProject();

        project.getPlugins().apply(HuskitContainersPlugin.class);

        var maxParallelUsages = List.copyOf(project.getGradle().getSharedServices().getRegistrations()).get(0).getMaxParallelUsages();
        assertThat(maxParallelUsages.isPresent()).isTrue();
        assertThat(maxParallelUsages.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("if there are two projects with plugin, then max parallel uses should be 2")
    void test_6() {
        var fixture = setupFixture();

        assertThat(fixture.maxParallelUsages.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("containers extension should be added")
    void test_7() {
        var project = setupProject();

        project.getPlugins().apply(HuskitContainersPlugin.class);
        evaluateProject(project);

        assertThat(project.getExtensions().findByName(ContainersExtension.name())).isNotNull();
        assertThat(project.getExtensions().findByType(ContainersExtension.class)).isNotNull();
    }

    @Test
    @DisplayName("adding mongo container with database name should set it")
    void test_8() {
        var project = setupProject();
        var dbName = "someDbName";

        project.getPlugins().apply(HuskitContainersPlugin.class);
        var containersExtension = (DockerContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
        containersExtension.mongo(mongo -> mongo.getDatabaseName().set(dbName));

        var requestedContainers = containersExtension.getContainersRequestedByUser().get();
        assertThat(requestedContainers).hasSize(1);
        var requestedContainer = (MongoContainerRequestedByUser) requestedContainers.get(0);
        assertThat(requestedContainer.getDatabaseName().get()).isEqualTo(dbName);
    }

    @Test
    @DisplayName("adding mongo container with image should set it")
    void test_9() {
        var project = setupProject();
        var img = "someImage";

        project.getPlugins().apply(HuskitContainersPlugin.class);
        var containersExtension = (DockerContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
        containersExtension.mongo(mongo -> mongo.getImage().set(img));

        var requestedContainers = containersExtension.getContainersRequestedByUser().get();
        assertThat(requestedContainers).hasSize(1);
        var requestedContainer = (MongoContainerRequestedByUser) requestedContainers.get(0);
        assertThat(requestedContainer.getImage().get()).isEqualTo(img);
    }

    @Test
    @DisplayName("adding mongo container with fixed port should set it")
    void test_10() {
        var project = setupProject();
        var port = 42;

        project.getPlugins().apply(HuskitContainersPlugin.class);
        var containersExtension = (DockerContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
        containersExtension.mongo(mongo -> mongo.getFixedPort().set(port));

        var requestedContainers = containersExtension.getContainersRequestedByUser().get();
        assertThat(requestedContainers).hasSize(1);
        var requestedContainer = (MongoContainerRequestedByUser) requestedContainers.get(0);
        assertThat(requestedContainer.getFixedPort().get()).isEqualTo(port);
    }

    @Test
    @DisplayName("if no containers added, then extension-requested container list is empty")
    void test_11() {
        var project = setupProject();

        project.getPlugins().apply(HuskitContainersPlugin.class);

        var containersExtension = (DockerContainersExtension) project.getExtensions().getByType(ContainersExtension.class);
        var requestedContainers = containersExtension.getContainersRequestedByUser().get();
        assertThat(requestedContainers).isEmpty();
    }

    private FixtureDockerWithParentProject setupFixture() {
        var delegateFixture = setupFixtureWithParentProject();
        delegateFixture.project().getPlugins().apply(HuskitContainersPlugin.class);
        delegateFixture.parentProject().getPlugins().apply(HuskitContainersPlugin.class);
        evaluateProject(delegateFixture.parentProject());
        evaluateProject(delegateFixture.project());
        var projectBuildServices = new ArrayList<>(delegateFixture.project().getGradle().getSharedServices().getRegistrations());
        assertThat(projectBuildServices).hasSize(1);
        var dockerBuildService = (ContainersBuildService) projectBuildServices.get(0).getService().get();
        var maxParallelUsages = projectBuildServices.get(0).getMaxParallelUsages();
        var dockerBuildServiceParams = dockerBuildService.getParameters();
        return new FixtureDockerWithParentProject(
                delegateFixture.project(),
                delegateFixture.parentProject(),
                delegateFixture.projectDir(),
                delegateFixture.parentProjectDir(),
                delegateFixture.objects(),
                delegateFixture.providers(),
                dockerBuildService,
                maxParallelUsages,
                dockerBuildServiceParams
        );
    }

    @RequiredArgsConstructor
    private static class FixtureDockerWithParentProject {

        Project project;
        Project parentProject;
        File projectDir;
        File parentProjectDir;
        ObjectFactory objects;
        ProviderFactory providers;
        ContainersBuildService dockerBuildService;
        Provider<Integer> maxParallelUsages;
        ContainersBuildServiceParams dockerBuildServiceParams;
    }
}
