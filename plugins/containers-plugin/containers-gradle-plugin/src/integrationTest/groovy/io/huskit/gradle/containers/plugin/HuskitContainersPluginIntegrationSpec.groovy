package io.huskit.gradle.containers.plugin

import groovy.transform.Canonical
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.huskit.containers.model.ContainerType
import io.huskit.containers.model.DefaultRequestedContainer
import io.huskit.containers.model.DefaultRequestedContainers
import io.huskit.containers.model.exception.NonUniqueContainerException
import io.huskit.containers.model.id.DefaultContainerId
import io.huskit.containers.model.image.DefaultContainerImage
import io.huskit.containers.model.port.DynamicContainerPort
import io.huskit.containers.model.request.DefaultMongoRequestedContainer
import io.huskit.gradle.commontest.BaseIntegrationSpec
import io.huskit.gradle.containers.plugin.api.ContainersExtension
import io.huskit.gradle.containers.plugin.api.MongoContainerRequestedByUser
import io.huskit.gradle.containers.plugin.internal.ContainersBuildServiceParams
import io.huskit.gradle.containers.plugin.internal.DockerContainersExtension
import io.huskit.gradle.containers.plugin.internal.buildservice.ContainersBuildService
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

class HuskitContainersPluginIntegrationSpec extends BaseIntegrationSpec {

    def "should create build service"() {
        given:
        def project = setupProject()

        when:
        project.plugins.apply(HuskitContainersPlugin)

        then:
        def buildServices = project.gradle.sharedServices.registrations.toList()
        buildServices.size() == 1
        buildServices[0].name == ContainersBuildService.name()
    }

    def "if build service already added, then no error thrown"() {
        given:
        def project = setupProject()

        when:
        project.gradle.sharedServices.registerIfAbsent(
                ContainersBuildService.name(),
                ContainersBuildService,
                { params -> }
        )
        project.plugins.apply(HuskitContainersPlugin)

        then:
        def buildServices = project.gradle.sharedServices.registrations.toList()
        buildServices.size() == 1
        buildServices[0].name == ContainersBuildService.name()
    }

    def "if parent project already created build service, then no error thrown"() {
        given:
        def fixture = setupFixtureWithParentProject()

        when:
        fixture.parentProject.plugins.apply(HuskitContainersPlugin)
        fixture.project.plugins.apply(HuskitContainersPlugin)

        then:
        def projectBuildServices = fixture.project.gradle.sharedServices.registrations.toList()
        def parentProjectBuildServices = fixture.parentProject.gradle.sharedServices.registrations.toList()
        projectBuildServices.size() == 1
        parentProjectBuildServices.size() == 1
        projectBuildServices[0] == parentProjectBuildServices[0]
        projectBuildServices[0].name == ContainersBuildService.name()
    }

    def "if parent project already created build service, and build service evaluated, then no error thrown"() {
        given:
        def fixture = setupFixtureWithParentProject()

        when:
        fixture.parentProject.plugins.apply(HuskitContainersPlugin)
        fixture.project.plugins.apply(HuskitContainersPlugin)
        evaluateProject(fixture.parentProject)
        evaluateProject(fixture.project)
        def projectBuildServices = fixture.project.gradle.sharedServices.registrations.toList()
        def parentProjectBuildServices = fixture.parentProject.gradle.sharedServices.registrations.toList()
        def projectBuildService = projectBuildServices[0].getService().getOrNull()
        def parentProjectBuildService = parentProjectBuildServices[0].getService().getOrNull()

        then:
        projectBuildService != null
        parentProjectBuildService != null
        projectBuildService == parentProjectBuildService
    }

    def "when build service evaluated, no exception thrown"() {
        given:
        def project = setupProject()

        when:
        project.plugins.apply(HuskitContainersPlugin)
        evaluateProject(project)
        def buildService = project.gradle.sharedServices.registrations.toList()[0].getService().getOrNull()

        then:
        buildService != null
    }

    def "if there is one project with plugin, then max parallel uses should be 1"() {
        given:
        def project = setupProject()

        when:
        project.plugins.apply(HuskitContainersPlugin)


        then:
        def maxParallelUsages = project.gradle.sharedServices.registrations.toList()[0].maxParallelUsages
        maxParallelUsages.isPresent()
        maxParallelUsages.get() == 1
    }

    def "if there are two projects with plugin, then max parallel uses should be 2"() {
        given:
        def fixture = setupFixture()

        expect:
        fixture.maxParallelUsages.get() == 2
    }

//    def "if mongo container is requested from build service, then it should return it"() {
//        given:
//        def fixture = setupFixture()
//        def containerId = "someContainerId"
//
//        when:
//        def containers = fixture.dockerBuildService.containers(
//                new DefaultRequestedContainers(
//                        new DefaultMongoRequestedContainer(
//                                new DefaultRequestedContainer(
//                                        new DefaultContainerImage("mongo:4.4.3"),
//                                        new DefaultContainerId(
//                                                containerId
//                                        ),
//                                        ContainerType.MONGO,
//                                        new DynamicContainerPort()
//                                )
//                        )
//                )
//        )
//        then:
//        def startedContainers = containers.start().list()
//        startedContainers.size() == 1
//        startedContainers[0].id().value() == containerId
//    }
//
//    def "if two mongo containers with same id requested from build service, then exception is thrown"() {
//        given:
//        def fixture = setupFixture()
//        def containerId = "someContainerId"
//        def containerType = ContainerType.MONGO
//        def requestedContainer = new DefaultMongoRequestedContainer(
//                new DefaultRequestedContainer(
//                        new DefaultContainerImage("anyImg"),
//                        new DefaultContainerId(
//                                containerId
//                        ),
//                        containerType,
//                        new DynamicContainerPort()
//                )
//        )
//
//
//        when:
//        fixture.dockerBuildService.containers(
//                new DefaultRequestedContainers([
//                        requestedContainer,
//                        requestedContainer
//                ])
//        ).start()
//
//        then:
//        def exception = thrown(NonUniqueContainerException)
//        exception.message.contains(containerId)
//        exception.message.contains(containerType.name())
//    }
//
//    def "if unknown container type requested, then exception is thrown"() {
//        given:
//        def fixture = setupFixture()
//        def containerId = "someContainerId"
//        def containerType = ContainerType.UNKNOWN
//        def requestedContainer = new DefaultRequestedContainer(
//                new DefaultContainerImage("anyImg"),
//                new DefaultContainerId(
//                        containerId
//                ),
//                containerType,
//                new DynamicContainerPort()
//        )
//
//        when:
//        fixture.dockerBuildService.containers(
//                new DefaultRequestedContainers([
//                        requestedContainer
//                ])
//        ).start().list()
//
//        then:
//        def exception = thrown(io.huskit.containers.model.exception.UnknownContainerTypeException)
//        exception.message.contains(containerType.name())
//    }

    def "containers extension should be added"() {
        given:
        def project = setupProject()

        when:
        project.plugins.apply(HuskitContainersPlugin)
        evaluateProject(project)

        then:
        project.extensions.findByName(ContainersExtension.name()) != null
        project.extensions.findByType(ContainersExtension) != null
    }

    def "adding mongo container with database name should set it"() {
        given:
        def project = setupProject()
        def dbName = 'someDbName'

        when:
        project.plugins.apply(HuskitContainersPlugin)
        def containersExtension = project.extensions.getByType(ContainersExtension) as DockerContainersExtension
        containersExtension.mongo({
            databaseName = dbName
        })

        then:
        def requestedContainers = containersExtension.containersRequestedByUser.get()
        requestedContainers.size() == 1
        def requestedContainer = requestedContainers[0] as MongoContainerRequestedByUser
        requestedContainer.databaseName.get() == dbName
    }

    def "adding mongo container with image should set it"() {
        given:
        def project = setupProject()
        def img = 'someImage'

        when:
        project.plugins.apply(HuskitContainersPlugin)
        def containersExtension = project.extensions.getByType(ContainersExtension) as DockerContainersExtension
        containersExtension.mongo({
            image = img
        })

        then:
        def requestedContainers = containersExtension.containersRequestedByUser.get()
        requestedContainers.size() == 1
        def requestedContainer = requestedContainers[0] as MongoContainerRequestedByUser
        requestedContainer.image.get() == img
    }

    def "adding mongo container with fixed port should set it"() {
        given:
        def project = setupProject()
        def port = 42

        when:
        project.plugins.apply(HuskitContainersPlugin)
        def containersExtension = project.extensions.getByType(ContainersExtension) as DockerContainersExtension
        containersExtension.mongo({
            fixedPort = port
        })

        then:
        def requestedContainers = containersExtension.containersRequestedByUser.get()
        requestedContainers.size() == 1
        def requestedContainer = requestedContainers[0] as MongoContainerRequestedByUser
        requestedContainer.fixedPort.get() == port
    }

    def "if no containers added, then extension-requested container list is empty"() {
        given:
        def project = setupProject()

        when:
        project.plugins.apply(HuskitContainersPlugin)

        then:
        def containersExtension = project.extensions.getByType(ContainersExtension) as DockerContainersExtension
        def requestedContainers = containersExtension.containersRequestedByUser.get()
        requestedContainers.size() == 0
    }

    def "if shouldStartBefore is set as task in extension, then"() {
        given:
        def project = setupProject()

        when:
        1 == 1

        then:
        1 == 1
    }

    private FixtureDockerWithParentProject setupFixture() {
        def delegateFixture = setupFixtureWithParentProject()
        delegateFixture.project.plugins.apply(HuskitContainersPlugin)
        delegateFixture.parentProject.plugins.apply(HuskitContainersPlugin)
        evaluateProject(delegateFixture.parentProject)
        evaluateProject(delegateFixture.project)
        def projectBuildServices = delegateFixture.project.gradle.sharedServices.registrations.toList()
        assert projectBuildServices.size() == 1
        def dockerBuildService = projectBuildServices[0].getService().get() as ContainersBuildService
        def maxParallelUsages = projectBuildServices[0].maxParallelUsages
        def dockerBuildServiceParams = dockerBuildService.parameters
        return new FixtureDockerWithParentProject(
                delegateFixture.project,
                delegateFixture.parentProject,
                delegateFixture.projectDir,
                delegateFixture.parentProjectDir,
                delegateFixture.objects,
                delegateFixture.providers,
                dockerBuildService,
                maxParallelUsages,
                dockerBuildServiceParams
        )
    }

    @Canonical
    @CompileStatic
    @TupleConstructor
    private static class FixtureDockerWithParentProject {

        final Project project
        final Project parentProject
        final File projectDir
        final File parentProjectDir
        final ObjectFactory objects
        final ProviderFactory providers
        final ContainersBuildService dockerBuildService
        final Provider<Integer> maxParallelUsages
        final ContainersBuildServiceParams dockerBuildServiceParams
    }
}
