package io.huskit;

import io.huskit.containers.model.DefaultRequestedContainers;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.model.port.DynamicContainerPort;
import io.huskit.containers.model.request.ContainersRequest;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import io.huskit.gradle.containers.core.ContainersApplication;
import io.huskit.log.fake.FakeLog;
import lombok.RequiredArgsConstructor;

import java.util.List;

public interface ContainersIntegrationTest extends DockerIntegrationTest {

    default void runFixture(ThrowingConsumer<Fixture> fixtureConsumer) {
        FakeLog log = new FakeLog();
        try (var application = ContainersApplication.application(log)) {
            fixtureConsumer.accept(
                    new Fixture(
                            log,
                            application
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default ContainersRequest prepareContainerRequest(FakeLog log, ProjectDescription projectDescription, List<RequestedContainer> requestedContainers) {
        return new ContainersRequest(
                projectDescription,
                new DefaultRequestedContainers(requestedContainers),
                log
        );
    }

    default ContainersRequest prepareContainerRequest(FakeLog log, List<RequestedContainer> requestedContainers) {
        return prepareContainerRequest(
                log,
                new ProjectDescription.Default("someRootProjectName", "someProjectPath", "someProjectName"),
                requestedContainers
        );
    }

    default ContainersRequest prepareContainerRequest(FakeLog log, RequestedContainer requestedContainer) {
        return prepareContainerRequest(
                log,
                List.of(requestedContainer)
        );
    }

    @RequiredArgsConstructor
    class Fixture {

        public final FakeLog log;
        public final DynamicContainerPort port = new DynamicContainerPort();
        public final ContainersApplication application;
    }
}