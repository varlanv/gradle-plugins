package io.huskit;

import io.huskit.containers.model.DefaultRequestedContainers;
import io.huskit.containers.model.ProjectDescription;
import io.huskit.containers.model.port.DynamicContainerPort;
import io.huskit.containers.model.request.ContainersRequest;
import io.huskit.containers.model.request.RequestedContainer;
import io.huskit.gradle.commontest.BaseDockerIntegrationTest;
import io.huskit.gradle.containers.plugin.GradleProjectDescription;
import io.huskit.gradle.containers.plugin.internal.ContainersApplication;
import io.huskit.log.fake.FakeLog;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Consumer;

public abstract class BaseContainersIntegrationTest extends BaseDockerIntegrationTest {

    protected void test(Consumer<Fixture> fixtureConsumer) {
        try (var application = ContainersApplication.application()) {
            fixtureConsumer.accept(
                    new Fixture(
                            application
                    )
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ContainersRequest prepareContainerRequest(FakeLog log, ProjectDescription projectDescription, List<RequestedContainer> requestedContainers) {
        return new ContainersRequest(
                projectDescription,
                new DefaultRequestedContainers(requestedContainers),
                log
        );
    }

    protected ContainersRequest prepareContainerRequest(FakeLog log, List<RequestedContainer> requestedContainers) {
        return prepareContainerRequest(
                log,
                new GradleProjectDescription("someRootProjectName", "someProjectPath", "someProjectName"),
                requestedContainers
        );
    }

    protected ContainersRequest prepareContainerRequest(FakeLog log, RequestedContainer requestedContainer) {
        return prepareContainerRequest(
                log,
                List.of(requestedContainer)
        );
    }

    @RequiredArgsConstructor
    public static class Fixture {

        public final FakeLog log = new FakeLog();
        public final String id = "someContainerId";
        public final String source = "someSource";
        public final DynamicContainerPort port = new DynamicContainerPort();
        public final ContainersApplication application;
    }
}
