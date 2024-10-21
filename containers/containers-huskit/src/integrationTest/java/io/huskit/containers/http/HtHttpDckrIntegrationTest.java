package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.common.Sneaky;
import io.huskit.common.function.MemoizedSupplier;
import io.huskit.common.port.DynamicContainerPort;
import io.huskit.common.port.MappedPort;
import io.huskit.containers.api.container.HtContainer;
import io.huskit.containers.api.container.HtContainerStatus;
import io.huskit.containers.api.container.logs.LookFor;
import io.huskit.gradle.commontest.DockerImagesStash;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(HtHttpDckrIntegrationTest.HtHttpDckrTestExtension.class)
class HtHttpDckrIntegrationTest implements DockerIntegrationTest {

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    @Execution(ExecutionMode.CONCURRENT)
    void listContainers__withRandomFilters__shouldReturnEmptyList(HtHttpDckr subject) {
        var htContainers = subject.containers().list(spec -> spec
                        .withIdFilter("asd")
                        .withLabelFilter("key", "val")
                        .withLabelFilter("key2")
                        .withNameFilter("someName")
                        .withAll()
                )
                .asList();

        assertThat(htContainers).isEmpty();
    }

    @Test
    @Disabled
    @EnabledOnOs(OS.WINDOWS)
    @Execution(ExecutionMode.CONCURRENT)
    void docker_alpine_container_spec(HtHttpDckr subject) {
        var containerRef = Mutable.<HtContainer>of();
        var ex = Mutable.<Throwable>of();
        try {
            var containerEnv = Map.of(
                    "ENV1", "ENVVALUE1",
                    "ENV2", "ENVVALUE2"
            );
            var containerLabels = Map.of(
                    "LABEL1", "LABELVALUE1",
                    "LABEL2", "LABELVALUE2"
            );
            var mappedPort1 = new MappedPort(new DynamicContainerPort().hostValue(), 80);
            var mappedPort2 = new MappedPort(new DynamicContainerPort().hostValue(), 8080);
            {
                var container = subject.containers().run(
                                DockerImagesStash.defaultSmall(),
                                spec -> spec
                                        .withCommand(
                                                DockerImagesStash.smallImageBusyCommand().command(),
                                                DockerImagesStash.smallImageBusyCommand().args()
                                        )
                                        .withEnv(containerEnv)
                                        .withLabels(containerLabels)
                                        .withPortBindings(Map.of(
                                                mappedPort1.host(), mappedPort1.container(),
                                                mappedPort2.host(), mappedPort2.container()
                                        ))
                                        .withLookFor("Hello World 123", Duration.ofSeconds(10)))
                        .exec();
                assertThat(container.id()).isNotEmpty();
                containerRef.set(container);
                var containerConfig = container.config();
                assertThat(containerConfig.labels()).containsAllEntriesOf(containerLabels);
                assertThat(containerConfig.env()).containsAllEntriesOf(containerEnv);
                assertThat(container.name()).isNotEmpty();
                assertThat(container.ports().get(0)).isIn(mappedPort1, mappedPort2).isNotEqualTo(container.ports().get(1));
                assertThat(container.ports().get(1)).isIn(mappedPort1, mappedPort2);
            }
            {
                var logs = new CopyOnWriteArrayList<String>();
                subject.containers().logs(containerRef.require().id())
                        .follow()
                        .lookFor(LookFor.lineMatching(line -> {
                            logs.add(line);
                            return line.contains("Hello World 123");
                        }));
                assertThat(logs).containsExactly("Hello World 1", "Hello World 123");
            }
            {
                var logs = new CopyOnWriteArrayList<>();
                subject.containers().logs(containerRef.require().id())
                        .follow()
                        .lookFor(LookFor.lineMatching(line -> {
                            logs.add(line);
                            return line.contains("Hello World 1");
                        }));
                assertThat(logs).containsExactly("Hello World 1");
            }

            {
                Runnable readLogs = () -> {
                    var logs = subject.containers()
                            .logs(containerRef.require().id())
                            .stream().all()
                            .collect(Collectors.toList());
                    assertThat(logs).containsExactly("Hello World 1", "Hello World 123");
                };
                readLogs.run();
                readLogs.run();
            }
            {
                subject.containers()
                        .logs(containerRef.require().id())
                        .asyncStream()
                        .thenAccept(logs -> assertThat(logs.all()).containsExactly("Hello World 1", "Hello World 123"))
                        .join();
            }
            {
                subject.containers()
                        .logs(containerRef.require().id())
                        .asyncStdOut()
                        .thenAccept(logs -> assertThat(logs.collect(Collectors.toList())).containsExactly("Hello World 1", "Hello World 123"))
                        .join();
            }
            {
                subject.containers()
                        .logs(containerRef.require().id())
                        .asyncStdErr()
                        .thenAccept(logs -> assertThat(logs.collect(Collectors.toList())).isEmpty())
                        .join();
            }
            {
                var logs = subject.containers()
                        .logs(containerRef.require().id())
                        .stdErr();
                assertThat(logs.collect(Collectors.toList())).isEmpty();
            }
            {
                var logs = subject.containers()
                        .logs(containerRef.require().id())
                        .stdErr();
                assertThat(logs.collect(Collectors.toList())).isEmpty();
            }
            {
                var inspected = subject.containers().inspect(containerRef.require().id());
                {
                    assertThat(inspected.id()).isEqualTo(containerRef.require().id());
                    assertThat(inspected.name()).isNotEmpty();
                    assertThat(inspected.createdAt()).is(today());
                    assertThat(inspected.args()).containsExactlyElementsOf(DockerImagesStash.smallImageBusyCommand().args());
                    assertThat(inspected.path()).isEqualTo(DockerImagesStash.smallImageBusyCommand().command());
                    assertThat(inspected.processLabel()).isEmpty();
                    assertThat(inspected.platform()).isEqualTo("linux");
                    assertThat(inspected.driver()).isNotEmpty();
                    assertThat(inspected.hostsPath()).isNotEmpty();
                    assertThat(inspected.hostnamePath()).isNotEmpty();
                    assertThat(inspected.restartCount()).isZero();
                    assertThat(inspected.mountLabel()).isEmpty();
                    assertThat(inspected.resolvConfPath()).isNotEmpty();
                    assertThat(inspected.logPath()).isNotEmpty();
                }
                {
                    var containerConfig = inspected.config();
                    assertThat(containerConfig.labels()).containsAllEntriesOf(containerLabels);
                    assertThat(containerConfig.env()).containsAllEntriesOf(containerEnv);
                    assertThat(containerConfig.cmd()).containsExactlyElementsOf(DockerImagesStash.smallImageBusyCommand().commandWithArgs());
                    assertThat(containerConfig.tty()).isFalse();
                    assertThat(containerConfig.attachStdin()).isFalse();
                    assertThat(containerConfig.attachStder()).isFalse();
                    assertThat(containerConfig.openStdin()).isFalse();
                    assertThat(containerConfig.entrypoint()).isEmpty();
                    assertThat(containerConfig.workingDir()).isEmpty();
                    assertThat(containerConfig.hostname()).isNotEmpty();
                }
                {
                    var containerNetwork = inspected.network();
                    assertThat(containerNetwork.gateway()).isNotEmpty();
                    assertThat(containerNetwork.ipAddress()).isNotEmpty();
                    assertThat(containerNetwork.ipPrefixLen()).isPositive();
                    assertThat(containerNetwork.macAddress()).isNotEmpty();
                    assertThat(containerNetwork.bridge()).isEmpty();
                    assertThat(containerNetwork.globalIpv6PrefixLen()).isZero();
                    assertThat(containerNetwork.globalIpv6Address()).isEmpty();
                    assertThat(containerNetwork.linkLocalIpv6Address()).isEmpty();
                    assertThat(containerNetwork.linkLocalIpv6PrefixLen()).isZero();
                    assertThat(containerNetwork.ipv6Gateway()).isEmpty();
                    assertThat(containerNetwork.hairpinMode()).isFalse();
                    assertThat(containerNetwork.endpointId()).isNotEmpty();
                    assertThat(containerNetwork.sandboxId()).isNotEmpty();
                    assertThat(containerNetwork.sandboxKey()).isNotEmpty();
                    assertThat(containerNetwork.secondaryIpAddresses()).isEmpty();
                    assertThat(containerNetwork.secondaryIpV6Addresses()).isEmpty();
                }
                {
                    var containerState = inspected.state();
                    assertThat(containerState.status()).isEqualTo(HtContainerStatus.RUNNING);
                    assertThat(containerState.pid()).isPositive();
                    assertThat(containerState.exitCode()).isZero();
                    assertThat(containerState.startedAt()).isNotNull();
                    assertThatThrownBy(containerState::finishedAt).hasMessageContaining("not yet finished");
                    assertThat(containerState.error()).isEmpty();
                    assertThat(containerState.running()).isTrue();
                    assertThat(containerState.paused()).isFalse();
                    assertThat(containerState.restarting()).isFalse();
                    assertThat(containerState.oomKilled()).isFalse();
                    assertThat(containerState.dead()).isFalse();
                }
                {
                    var containerGraphDriver = inspected.graphDriver();
                    assertThat(containerGraphDriver.data()).isNotEmpty();
                    assertThat(containerGraphDriver.name()).isNotEmpty();
                }
                {
                    var hostConfig = inspected.hostConfig();
                }
                {
                    subject.containers().execInContainer(
                            containerRef.require().id(),
                            "sh",
                            List.of("-c", "echo $((1 + 1)) && echo $((2 + 2))")
                    ).exec();
                }
            }
        } catch (Throwable t) {
            ex.set(t);
        } finally {
            try {
                containerRef.ifPresent(container -> subject.containers().remove(container.id(), spec -> spec.withForce().withVolumes()).exec());
            } finally {
                if (ex.isPresent()) {
                    var originalEx = ex.require();
                    Sneaky.rethrow(originalEx);
                }
            }
        }
    }

    static final class HtHttpDckrTestExtension implements ParameterResolver, AfterAllCallback {

        private static final MemoizedSupplier<HtHttpDckr> subjectSupplier = MemoizedSupplier.of(() -> new HtHttpDckr().withCleanOnClose(true));
        private static final ConcurrentMap<String, Supplier<Object>> parameters = new ConcurrentHashMap<>(
                Map.of(
                        HtHttpDckr.class.getName(), subjectSupplier::get
                )
        );

        @Override
        public void afterAll(ExtensionContext context) {
            if (subjectSupplier.isInitialized()) {
                subjectSupplier.get().close();
            }
        }

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return parameters.containsKey(parameterContext.getParameter().getType().getName());
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
            return Optional.ofNullable(parameters.get(parameterContext.getParameter().getType().getName()).get())
                    .orElseThrow(() -> new ParameterResolutionException("No supplier found for " + parameterContext.getParameter().getType().getName()));
        }
    }
}
