//package io.huskit.containers.cli;
//
//import io.huskit.common.Mutable;
//import io.huskit.common.function.MemoizedSupplier;
//import io.huskit.common.port.DynamicContainerPort;
//import io.huskit.common.port.MappedPort;
//import io.huskit.containers.api.container.HtContainer;
//import io.huskit.containers.api.container.HtContainerStatus;
//import io.huskit.containers.api.container.logs.HtLogs;
//import io.huskit.containers.api.container.logs.LookFor;
//import io.huskit.containers.api.container.run.HtRmSpec;
//import io.huskit.containers.api.docker.HtDocker;
//import io.huskit.gradle.commontest.DockerImagesStash;
//import io.huskit.gradle.commontest.DockerIntegrationTest;
//import io.huskit.gradle.commontest.ShellConditions;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.TestTemplate;
//import org.junit.jupiter.api.extension.*;
//import org.junit.jupiter.api.parallel.Execution;
//import org.junit.jupiter.api.parallel.ExecutionMode;
//
//import java.time.Duration;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentMap;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.function.BiFunction;
//import java.util.function.Predicate;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
/// **
// * Test suite using a shared {@link HtCliDocker} instance across most of the tests.
// *
// * <p>In some cases, this instance may be accessed concurrently by multiple tests.
// * While it's generally not recommended to share instances between tests due to potential
// * flakiness, {@link HtCliDocker} is designed to be thread-safe. Therefore, test flakiness
// * in this context can often indicate a bug in the implementation.</p>
// */
//@ExtendWith(HtCliDckrIntegrationTest.ShellExtension.class)
//class HtCliDckrIntegrationTest implements DockerIntegrationTest {
//
//    @TestTemplate
//    @Execution(ExecutionMode.CONCURRENT)
//    @DisplayName("Integration with alpine docker container should work")
//    void docker_alpine_container_spec(HtCliDocker subject) {
//        var containerRef = Mutable.<HtContainer>of();
//        try {
//            var containerEnv = Map.of(
//                    "ENV1", "ENVVALUE1",
//                    "ENV2", "ENVVALUE2"
//            );
//            var containerLabels = Map.of(
//                    "LABEL1", "LABELVALUE1",
//                    "LABEL2", "LABELVALUE2"
//            );
//            var mappedPort1 = new MappedPort(new DynamicContainerPort().hostValue(), 80);
//            var mappedPort2 = new MappedPort(new DynamicContainerPort().hostValue(), 8080);
//            {
//                var container = subject.containers().run(
//                                DockerImagesStash.defaultSmall(),
//                                spec -> spec
//                                        .withCommand(
//                                                DockerImagesStash.smallImageBusyCommand().command(),
//                                                DockerImagesStash.smallImageBusyCommand().args()
//                                        )
//                                        .withEnv(containerEnv)
//                                        .withLabels(containerLabels)
//                                        .withPortBindings(Map.of(
//                                                mappedPort1.host(), mappedPort1.container(),
//                                                mappedPort2.host(), mappedPort2.container()
//                                        ))
//                                        .withLookFor("Hello World 2", Duration.ofSeconds(10)))
//                        .exec();
//                assertThat(container.id()).isNotEmpty();
//                containerRef.set(container);
//                var containerConfig = container.config();
//                assertThat(containerConfig.labels()).containsAllEntriesOf(containerLabels);
//                assertThat(containerConfig.env()).containsAllEntriesOf(containerEnv);
//                assertThat(container.name()).isNotEmpty();
//                var containerNetwork = container.network();
//                assertThat(containerNetwork.ports().get(0)).isIn(mappedPort1, mappedPort2).isNotEqualTo(containerNetwork.ports().get(1));
//                assertThat(containerNetwork.ports().get(1)).isIn(mappedPort1, mappedPort2);
//            }
//            {
////                var logs = subject.containers().logs(containerRef.require().id())
////                        .follow()
////                        .lookFor(LookFor.word("Hello World 2"))
////                assertThat(logs).containsExactly("Hello World 1", "Hello World 2");
//            }
//
//            {
//                var logs = subject.containers().logs(containerRef.require().id())
//                        .stream()
//                        .all()
//                        .collect(Collectors.toList());
//                assertThat(logs).containsExactly("Hello World 1", "Hello World 2");
//            }
//            {
//                var inspected = subject.containers().inspect(containerRef.require().id());
//                {
//                    assertThat(inspected.id()).isEqualTo(containerRef.require().id());
//                    assertThat(inspected.name()).isNotEmpty();
//                    assertThat(inspected.createdAt()).is(today());
//                    assertThat(inspected.args()).containsExactlyElementsOf(DockerImagesStash.smallImageBusyCommand().args());
//                    assertThat(inspected.path()).isEqualTo(DockerImagesStash.smallImageBusyCommand().command());
//                    assertThat(inspected.processLabel()).isEmpty();
//                    assertThat(inspected.platform()).isEqualTo("linux");
//                    assertThat(inspected.driver()).isNotEmpty();
//                    assertThat(inspected.hostsPath()).isNotEmpty();
//                    assertThat(inspected.hostnamePath()).isNotEmpty();
//                    assertThat(inspected.restartCount()).isZero();
//                    assertThat(inspected.mountLabel()).isEmpty();
//                    assertThat(inspected.resolvConfPath()).isNotEmpty();
//                    assertThat(inspected.logPath()).isNotEmpty();
//                }
//                {
//                    var containerConfig = inspected.config();
//                    assertThat(containerConfig.labels()).containsAllEntriesOf(containerLabels);
//                    assertThat(containerConfig.env()).containsAllEntriesOf(containerEnv);
//                    assertThat(containerConfig.cmd()).containsExactlyElementsOf(DockerImagesStash.smallImageBusyCommand().commandWithArgs());
//                    assertThat(containerConfig.tty()).isFalse();
//                    assertThat(containerConfig.attachStdin()).isFalse();
//                    assertThat(containerConfig.attachStder()).isFalse();
//                    assertThat(containerConfig.openStdin()).isFalse();
//                    assertThat(containerConfig.entrypoint()).isEmpty();
//                    assertThat(containerConfig.workingDir()).isEmpty();
//                    assertThat(containerConfig.hostname()).isNotEmpty();
//                }
//                {
//                    var containerNetwork = inspected.network();
//                    assertThat(containerNetwork.ports()).containsExactly(mappedPort1, mappedPort2);
//                    assertThat(containerNetwork.gateway()).isNotEmpty();
//                    assertThat(containerNetwork.ipAddress()).isNotEmpty();
//                    assertThat(containerNetwork.ipPrefixLen()).isPositive();
//                    assertThat(containerNetwork.macAddress()).isNotEmpty();
//                    assertThat(containerNetwork.bridge()).isEmpty();
//                    assertThat(containerNetwork.globalIpv6PrefixLen()).isZero();
//                    assertThat(containerNetwork.globalIpv6Address()).isEmpty();
//                    assertThat(containerNetwork.linkLocalIpv6Address()).isEmpty();
//                    assertThat(containerNetwork.linkLocalIpv6PrefixLen()).isZero();
//                    assertThat(containerNetwork.ipv6Gateway()).isEmpty();
//                    assertThat(containerNetwork.hairpinMode()).isFalse();
//                    assertThat(containerNetwork.endpointId()).isNotEmpty();
//                    assertThat(containerNetwork.sandboxId()).isNotEmpty();
//                    assertThat(containerNetwork.sandboxKey()).isNotEmpty();
//                    assertThat(containerNetwork.secondaryIpAddresses()).isEmpty();
//                    assertThat(containerNetwork.secondaryIpV6Addresses()).isEmpty();
//                    assertThatThrownBy(containerNetwork::firstMappedPort).hasMessageContaining("multiple are present");
//                }
//                {
//                    var containerState = inspected.state();
//                    assertThat(containerState.status()).isEqualTo(HtContainerStatus.RUNNING);
//                    assertThat(containerState.pid()).isPositive();
//                    assertThat(containerState.exitCode()).isZero();
//                    assertThat(containerState.startedAt()).isNotNull();
//                    assertThatThrownBy(containerState::finishedAt).hasMessageContaining("not yet finished");
//                    assertThat(containerState.error()).isEmpty();
//                    assertThat(containerState.running()).isTrue();
//                    assertThat(containerState.paused()).isFalse();
//                    assertThat(containerState.restarting()).isFalse();
//                    assertThat(containerState.oomKilled()).isFalse();
//                    assertThat(containerState.dead()).isFalse();
//                }
//                {
//                    var containerGraphDriver = inspected.graphDriver();
//                    assertThat(containerGraphDriver.data()).isNotEmpty();
//                    assertThat(containerGraphDriver.name()).isNotEmpty();
//                }
//                {
//                    subject.containers().execInContainer(
//                            containerRef.require().id(),
//                            "sh",
//                            List.of("-c", "echo $((1 + 1)) && echo $((2 + 2))")
//                    ).exec();
//                    assertThat(subject.containers().logs(containerRef.require().id()).stream().all().collect(Collectors.toList()))
//                            .containsExactly("Hello World 1", "Hello World 2");
//                }
//            }
//        } finally {
//            containerRef.ifPresent(container -> subject.containers().remove(container.id(), spec -> spec.withForce().withVolumes()).exec());
//        }
//    }
//
////    @TestTemplate
////    @DisplayName("docker cli .stop method should stop docker shell")
////    void stop__should_stop_docker_shell(ShellType shellType) {
////        // given
////        var recorder = new ThreadLocalCliRecorder();
////        var subject = HtDocker.cli()
////                .withCleanOnClose(true)
////                .configure(spec ->
////                        spec.withCliRecorder(recorder)
////                                .withShell(shellType));
////        var containerId = subject.containers()
////                .run(DockerImagesStash.defaultSmall(), spec -> spec.withCommand("sh -c \"while true; do sleep 3600; done\""))
////                .exec()
////                .id();
////        Supplier<List<HtContainer>> findContainers = () -> subject.containers()
////                .list(spec -> spec.withIdFilter(containerId))
////                .asList();
////        assertThat(findContainers.get()).hasSize(1);
////
////        // when
////        subject.close();
////
////        // then
////        assertThatThrownBy(findContainers::get)
////                .isInstanceOf(IllegalStateException.class)
////                .hasMessageContaining("Cli is closed and  cannot be used anymore");
////
////        // and
////        var actual = HtDocker.cli()
////                .containers()
////                .list(spec -> spec.withIdFilter(containerId))
////                .asList();
////        assertThat(actual).isEmpty();
////    }
////
////    @TestTemplate
////    @DisplayName("container list withAll should create correct command")
////    void container_list__with_all__ok(HtCliDocker subject, ThreadLocalCliRecorder recorder) {
////        // given
////        var expectedFindIdsCommand = List.of(
////                "docker", "ps", "-a",
////                "--format", HtStrings.doubleQuote("{{json .}}"),
////                "--format", HtStrings.doubleQuote("{{.ID}}")
////        );
////        var containers = subject.containers().list().withArgs(HtListContainersArgsSpec::withAll)
////                .asList();
////
////        assertThat(containers).isNotNull();
////        assertThat(recorder.forCurrentThread().size()).isGreaterThanOrEqualTo(1);
////        assertThat(recorder.forCurrentThread().get(0).value()).isEqualTo(expectedFindIdsCommand);
////    }
////
////    @TestTemplate
////    @DisplayName("withFilter by id should create correct command")
////    void container_list__with_filter_by_id__ok(HtCliDocker subject, ThreadLocalCliRecorder recorder) {
////        var id = "SOME___Id__That__should_NOT__exist";
////        var expectedFindIdsCommand = List.of(
////                "docker",
////                "ps",
////                "--filter", HtStrings.doubleQuote("id=" + id),
////                "--format", HtStrings.doubleQuote("{{json .}}"),
////                "--format", HtStrings.doubleQuote("{{.ID}}")
////        );
////        var containers = subject.containers().list(spec -> spec.withIdFilter(id))
////                .asList();
////
////        assertThat(containers).isEmpty();
////        assertThat(recorder.forCurrentThread()).hasSize(1);
////        assertThat(recorder.forCurrentThread().get(0).value()).isEqualTo(expectedFindIdsCommand);
////    }
////
////    @TestTemplate
////    @DisplayName("list withFilter and withAll should create correct command")
////    void list__with_filter__and_all__ok(HtCliDocker subject, ThreadLocalCliRecorder recorder) {
////        var id = "SOME___Id__That__should_NOT__exist";
////        var containers = subject.containers().list(spec -> spec.withIdFilter(id).withAll())
////                .asList();
////
////        assertThat(containers).isNotNull();
////        assertThat(recorder.forCurrentThread().size()).isGreaterThanOrEqualTo(1);
////        assertThat(recorder.forCurrentThread().get(0).value()).isEqualTo(List.of(
////                        "docker",
////                        "ps",
////                        "-a",
////                        "--filter", HtStrings.doubleQuote("id=" + id),
////                        "--format", HtStrings.doubleQuote("{{json .}}"),
////                        "--format", HtStrings.doubleQuote("{{.ID}}")
////                )
////        );
////    }
////
////    @TestTemplate
////    @DisplayName("container run when not called exec then should not run command")
////    void run__when_not_called_exec__should_not_run_command(HtCliDocker subject, ThreadLocalCliRecorder recorder, String smallImage) {
////        // given
////        subject.containers().run(smallImage);
////
////        // then
////        assertThat(recorder.forCurrentThread()).isEmpty();
////    }
////
////    @TestTemplate
////    @DisplayName("with labels should create correct command")
////    void run__with_labels__should_create_correct_command(HtCliDocker subject, ThreadLocalCliRecorder recorder, String smallImage) {
////        // given
////        var labels = new LinkedHashMap<String, String>();
////        labels.put("someLabelKey", "someLabelVal");
////        labels.put("someLabelKey2", "someLabelVal2");
////        subject.containers().run(smallImage, spec -> spec.withLabels(labels).withRemove())
////                .exec();
////
////        // then
////        assertThat(recorder.forCurrentThread()).hasSize(1);
////        assertThat(recorder.forCurrentThread().get(0).value())
////                .containsExactly("docker", "run", "-d", "--rm",
////                        "--label", HtStrings.doubleQuote("someLabelKey=someLabelVal"),
////                        "--label", HtStrings.doubleQuote("someLabelKey2=someLabelVal2"),
////                        smallImage);
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `list` when finds one container by id should return correct container")
////    void list__when_finds_one_container_by_id__should_run_correct_commands(OneContainerFixture fixture) {
////        fixture.subject().containers().list(spec ->
////                        spec.withIdFilter(fixture.containerId()))
////                .asList();
////
////        assertThat(fixture.recorder().forCurrentThread()).hasSize(2);
////        assertThat(fixture.recorder().forCurrentThread().get(0).value())
////                .isEqualTo(List.of(
////                                "docker",
////                                "ps",
////                                "--filter", HtStrings.doubleQuote("id=" + fixture.containerId()),
////                                "--format", HtStrings.doubleQuote("{{json .}}"),
////                                "--format", HtStrings.doubleQuote("{{.ID}}")
////                        )
////                );
////        assertThat(fixture.recorder().forCurrentThread().get(1).value())
////                .isEqualTo(List.of("docker", "inspect", "--format", HtStrings.doubleQuote("{{json .}}"), fixture.trimmedId()));
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `list` when finds one container by id should return correct container")
////    void list__when_finds_one_container_by_id__should_return_correct_container(OneContainerFixture fixture) {
////        var containers = fixture.subject().containers().list(spec ->
////                        spec.withIdFilter(fixture.containerId()))
////                .asList();
////
////        assertThat(containers).hasSize(1);
////        fixture.verifyContainer(containers);
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `list` when finds one container by label should run correct commands")
////    void list__when_finds_one_container_by_label__should_run_correct_commands(OneContainerFixture fixture) {
////        fixture.subject().containers().list(spec ->
////                        spec.withLabelFilter(fixture.labelIdKey(), fixture.labelId()))
////                .asList();
////
////        assertThat(fixture.recorder().forCurrentThread()).hasSize(2);
////        assertThat(fixture.recorder().forCurrentThread().get(0).value())
////                .isEqualTo(List.of(
////                                "docker",
////                                "ps",
////                                "--filter",
////                                HtStrings.doubleQuote("label=" + fixture.labelIdKey() + "=" + fixture.labelId()),
////                                "--format", HtStrings.doubleQuote("{{json .}}"),
////                                "--format", HtStrings.doubleQuote("{{.ID}}")
////                        )
////                );
////        assertThat(fixture.recorder().forCurrentThread().get(1).value())
////                .isEqualTo(List.of(
////                                "docker",
////                                "inspect",
////                                "--format",
////                                HtStrings.doubleQuote("{{json .}}"), fixture.trimmedId()
////                        )
////                );
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `list` when finds one container by label should return correct container")
////    void list__when_finds_one_container_by_label__should_return_correct_container(OneContainerFixture fixture) {
////        var containers = fixture.subject().containers().list(spec -> spec.withLabelFilter(fixture.labelIdKey(), fixture.labelId()))
////                .asList();
////
////        assertThat(containers).hasSize(1);
////        fixture.verifyContainer(containers);
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `list` with all and filter by label and filter by id should run correct commands")
////    void list__with_all_and_filter_by_label_and_filter_by_id__should_run_correct_commands(OneContainerFixture fixture) {
////        fixture.subject().containers().list(spec ->
////                        spec.withAll()
////                                .withLabelFilter(fixture.labelIdKey(), fixture.labelId())
////                                .withIdFilter(fixture.containerId()))
////                .asList();
////
////        assertThat(fixture.recorder().forCurrentThread()).hasSize(2);
////        assertThat(fixture.recorder().forCurrentThread().get(0).value())
////                .isEqualTo(List.of(
////                                "docker", "ps", "-a",
////                                "--filter", HtStrings.doubleQuote("id=" + fixture.containerId()),
////                                "--filter", HtStrings.doubleQuote("label=" + fixture.labelIdKey() + "=" + fixture.labelId()),
////                                "--format", HtStrings.doubleQuote("{{json .}}"),
////                                "--format", HtStrings.doubleQuote("{{.ID}}")
////                        )
////                );
////        assertThat(fixture.recorder().forCurrentThread().get(1).value())
////                .isEqualTo(List.of(
////                                "docker",
////                                "inspect",
////                                "--format", HtStrings.doubleQuote("{{json .}}"), fixture.trimmedId()
////                        )
////                );
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `list` with all and filter by label and filter by id should return correct container")
////    void list__with_all_and_filter_by_label_and_filter_by_id__should_return_correct_container(OneContainerFixture fixture) {
////        var containers = fixture.subject().containers().list(spec ->
////                        spec.withAll()
////                                .withLabelFilter(fixture.labelIdKey(), fixture.labelId())
////                                .withIdFilter(fixture.containerId()))
////                .asList();
////
////        assertThat(containers).hasSize(1);
////        fixture.verifyContainer(containers);
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` should return log messages")
////    void logs__should_return_log_messages(OneContainerFixture fixture) {
////        var logs = fixture.subject().containers().logs(fixture.containerId());
////
////        fixture.verifyLogMessages(logs);
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` should create correct command")
////    void logs__should_create_correct_command(OneContainerFixture fixture) {
////        fixture.subject().containers().logs(fixture.containerId())
////                .stream()
////                .collect(Collectors.toList());
////
////        assertThat(fixture.recorder().forCurrentThread()).hasSize(1);
////        assertThat(fixture.recorder().forCurrentThread().get(0).value())
////                .isEqualTo(List.of("docker", "logs", fixture.containerId()));
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` if didn't consume stream should not run command")
////    void logs__if_didnt_consume_stream__should_not_run_command(OneContainerFixture fixture) {
////        fixture.subject().containers().logs(fixture.containerId())
////                .stream()
////                .map(String::length)
////                .filter(i -> i > 0);
////
////        assertThat(fixture.recorder().forCurrentThread()).isEmpty();
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` with follow and `lookFor` spec should return first log message")
////    void logs__withFollow_lookFor__should_return_first_log_message(OneContainerFixture fixture) {
////        var logs = fixture.subject().containers().logs(fixture.containerId()).follow().lookFor(LookFor.word("Hello"));
////
////        var logsList = logs.stream().collect(Collectors.toList());
////
////        assertThat(logsList).containsExactly("Hello World 1");
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` with follow and `lookFor` spec should return all log messages up to first match")
////    void logs__withFollow_lookFor__should_return_all_log_messages_up_to_first_match(OneContainerFixture fixture) {
////        var logs = fixture.subject().containers().logs(fixture.containerId())
////                .follow()
////                .lookFor(LookFor.word("Hello World 2"));
////
////        var logsList = logs.stream().collect(Collectors.toList());
////
////        assertThat(logsList).containsExactly("Hello World 1", "Hello World 2");
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` with follow and `lookFor` spec should create correct command")
////    void logs__withFollow_lookFor__should_create_correct_command(OneContainerFixture fixture) {
////        fixture.subject().containers().logs(fixture.containerId())
////                .follow()
////                .lookFor(LookFor.word("Hello"))
////                .stream()
////                .collect(Collectors.toList());
////
////        assertThat(fixture.recorder().forCurrentThread()).hasSize(1);
////        assertThat(fixture.recorder().forCurrentThread().get(0).value())
////                .isEqualTo(List.of("docker", "logs", "-f", fixture.containerId()));
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, `logs` with follow when can't find within timeout should throw exception")
////    void logs__withFollow_lookFor_withTimout_cant_find__should_throw_exception(OneContainerFixture fixture) {
////        assertThatThrownBy(() -> fixture.subject().containers().logs(fixture.containerId()).follow()
////                .lookFor(LookFor.word("nonExistentWord").withTimeout(Duration.ofMillis(20)))
////                .stream()
////                .collect(Collectors.toList()))
////                .isInstanceOf(TimeoutException.class);
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, images list should contain container image")
////    void image_list_should_return_container_image(OneContainerFixture fixture) {
////        var actual = fixture.subject().images().list(spec -> spec.withFilterByReference(DockerImagesStash.defaultSmall()))
////                .stream()
////                .collect(Collectors.toList());
////
////        assertThat(actual).isNotEmpty();
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, images list should return images with short ids")
////    void image_list_should_return_images_with_short_ids(OneContainerFixture fixture) {
////        var actual = fixture.subject().images().list()
////                .stream()
////                .map(HtImageView::shortId)
////                .collect(Collectors.toList());
////
////        assertThat(actual).isNotEmpty();
////        assertThat(actual).allSatisfy(id -> assertThat(id).hasSize(12));
////    }
////
////    @TestTemplate
////    @DisplayName("when one container, images list should return images with full ids")
////    void image_list_should_return_images_with_full_ids(OneContainerFixture fixture) {
////        var actual = fixture.subject().images().list(spec -> spec.withFilterByReference(DockerImagesStash.defaultSmall()))
////                .stream()
////                .map(HtImageView::inspect)
////                .map(HtImageRichView::id)
////                .collect(Collectors.toList());
////
////        assertThat(actual).isNotEmpty();
////        assertThat(actual).allSatisfy(id -> assertThat(id).hasSize(64));
////    }
////
////    @TestTemplate
////    @DisplayName("'image pull' should successfully pull image")
////    void image_pull_should_successfully_pull_image(HtCliDocker subject, String smallImage) {
////        subject.images().pull(smallImage).exec();
////
////        var existingImages = subject.images().list(spec -> spec.withFilterByReference(smallImage))
////                .stream()
////                .collect(Collectors.toList());
////
////        assertThat(existingImages).isNotEmpty();
////    }
////
////    @TestTemplate
////    @DisplayName("'image rm' should successfully remove image")
////    void image_rm_should_successfully_remove_image(HtCliDocker subject, String smallImage) {
////        subject.images().pull(smallImage).exec();
////        subject.images().rm(smallImage).exec();
////
////        var existingImages = subject.images().list(spec -> spec.withFilterByReference(smallImage))
////                .stream()
////                .collect(Collectors.toList());
////
////        assertThat(existingImages).isEmpty();
////    }
////
////    @TestTemplate
////    @DisplayName("'volume create' should create volume")
////    void volume_create_should_create_volume(HtCliDocker subject) {
////        // when creating volume
////        var volumes = subject.volumes();
////        var volumeId = String.valueOf(System.nanoTime());
////        var createdVolumeId = volumes.create(volumeId, spec -> spec
////                        .withLabel("label1")
////                        .withLabel("label2", "value")
////                        .withLabels(Map.of("label3", "value", "label4", "value"))
////                )
////                .exec();
////        assertThat(createdVolumeId).isEqualTo(volumeId);
////
////        // then volume id should not be empty
////        assertThat(volumeId).isNotEmpty();
////        boolean removed = false;
////        try {
////            // and volume should be found with inspect command
////            var volumeView = volumes.inspect(volumeId);
////            assertThat(volumeView.id()).isEqualTo(volumeId);
////            assertThat(volumeView.labels()).containsAllEntriesOf(
////                    Map.of("label1", "", "label2", "value", "label3", "value", "label4", "value")
////            );
////            assertThat(volumeView.createdAt()).is(today());
////
////            // and volume should be found with list command
////            var existingVolumesBeforeRm = volumes.list().stream()
////                    .map(HtVolumeView::id)
////                    .collect(Collectors.toSet());
////            assertThat(existingVolumesBeforeRm).contains(volumeId);
////
////            // when removing volume
////            volumes.rm(volumeId, true).exec();
////            removed = true;
////
////            // then volume should not be found with list command
////            var listedVolumeAfterRm = volumes.list().stream()
////                    .filter(volume -> volume.id().equals(volumeId))
////                    .findFirst();
////            assertThat(listedVolumeAfterRm).isEmpty();
////        } finally {
////            if (!removed) {
////                volumes.rm(volumeId, true).exec();
////            }
////        }
////    }
//
//    @Getter
//    @RequiredArgsConstructor
//    static class OneContainerFixture {
//
//        HtCliDocker subject;
//        ThreadLocalCliRecorder recorder;
//        String labelIdKey;
//        String labelId;
//        Map<String, String> labels;
//        String containerId;
//        String trimmedId;
//
//        void verifyContainer(List<HtContainer> containers) {
//            var actual = containers.get(0);
//            assertThat(actual.id()).isEqualTo(containerId);
//            assertThat(actual.config().labels()).isEqualTo(labels);
//        }
//
//        void verifyLogMessages(HtLogs logs) {
//            var logsList = logs.stream().all().collect(Collectors.toList());
//
//            assertThat(logsList).containsExactly("Hello World 1", "Hello World 2");
//        }
//    }
//
//    static class ShellExtension implements TestTemplateInvocationContextProvider, AfterAllCallback {
//
//        private static final ConcurrentMap<ShellType, Context> contexts = new ConcurrentHashMap<>();
//
//        @Override
//        public void afterAll(ExtensionContext extensionContext) {
//            contexts.values().forEach(context -> {
//                if (context.oneContainerFixture.isInitialized()) {
//                    context.subject.containers().remove(context.oneContainerFixture.get().containerId, HtRmSpec::withForce).exec();
//                }
//                context.subject.close();
//            });
//        }
//
//        @RequiredArgsConstructor
//        static class Context {
//
//            ShellType shellType;
//            HtCliDocker subject;
//            ThreadLocalCliRecorder recorder;
//            MemoizedSupplier<OneContainerFixture> oneContainerFixture = MemoizedSupplier.of(this::buildOneContainerFixture);
//
//            private OneContainerFixture buildOneContainerFixture() {
//                var labelIdKey = "labelId";
//                var labelId = "some_very_unique_id_that_should_exist_only_in_this_test" + ThreadLocalRandom.current().nextDouble();
//                var labels = Map.of(
//                        labelIdKey, labelId,
//                        "someLabelKey1", "someLabelVal1",
//                        "someLabelKey2", "someLabelVal2"
//                );
//                var container = subject.containers().run(
//                        DockerImagesStash.defaultSmall(),
//                        spec -> spec
//                                .withCommand(
//                                        DockerImagesStash.smallImageBusyCommand().command(),
//                                        DockerImagesStash.smallImageBusyCommand().args()
//                                )
//                                .withLabels(labels)
//                ).exec();
//                var containerId = container.id();
//                assertThat(container).isNotNull();
//                assertThat(containerId).isNotBlank();
//                assertThat(container.config().labels()).isEqualTo(labels);
//                recorder.forCurrentThread().clear();
//                var trimmedId = containerId.substring(0, 12);
//                return new OneContainerFixture(
//                        subject,
//                        recorder,
//                        labelIdKey,
//                        labelId,
//                        labels,
//                        containerId,
//                        trimmedId
//                );
//            }
//        }
//
//        @Override
//        public boolean supportsTestTemplate(ExtensionContext context) {
//            return true;
//        }
//
//        @Override
//        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
//            var predicates = Map.<ShellType, Predicate<ShellType>>of(
//                    ShellType.DEFAULT, shellType -> false,
//                    ShellType.BASH, shellType -> ShellConditions.bashAvailable(),
//                    ShellType.POWERSHELL, shellType -> ShellConditions.powershellAvailable(),
//                    ShellType.CMD, shellType -> ShellConditions.cmdAvailable(),
//                    ShellType.SH, shellType -> ShellConditions.shAvailable()
//            );
//            return Arrays.stream(ShellType.values())
//                    .filter(predicates::containsKey)
//                    .filter(shellType -> predicates.get(shellType).test(shellType))
//                    .map(shellType -> contexts.computeIfAbsent(shellType, k -> {
//                        var recorder = new ThreadLocalCliRecorder();
//                        return new Context(
//                                shellType,
//                                HtDocker.cli().configure(spec -> spec
//                                        .withCliRecorder(recorder)
//                                        .withShell(shellType)
//                                        .withForwardStderr(true)
//                                        .withForwardStdout(true)
//                                ),
//                                recorder
//                        );
//                    }))
//                    .map(context -> new TestTemplateInvocationContext() {
//
//                        @Override
//                        public String getDisplayName(int invocationIndex) {
//                            return context.shellType.name();
//                        }
//
//                        @Override
//                        public List<Extension> getAdditionalExtensions() {
//                            var parameterMap = Map.<String, BiFunction<ExtensionContext, Context, Object>>of(
//                                    ThreadLocalCliRecorder.class.getName(), (ectx, ctx) -> ctx.recorder,
//                                    HtCliDocker.class.getName(), (ectx, ctx) -> ctx.subject,
//                                    ShellType.class.getName(), (ectx, ctx) -> ctx.shellType,
//                                    OneContainerFixture.class.getName(), (ectx, ctx) -> ctx.oneContainerFixture.get(),
//                                    String.class.getName(), (ectx, ctx) -> DockerImagesStash.pickSmall(ectx.getUniqueId())
//                            );
//                            return List.of(
//                                    (BeforeEachCallback) extensionContext -> {
//                                        Optional.ofNullable(contexts.get(context.shellType)).ifPresent(context ->
//                                                context.recorder.clearForCurrentThread());
//                                    },
//                                    (AfterEachCallback) extensionContext -> {
//                                        DockerImagesStash.returnSmallIfPresent(extensionContext.getUniqueId());
//                                    },
//                                    new ParameterResolver() {
//
//                                        @Override
//                                        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
//                                            return parameterMap.containsKey(parameterContext.getParameter().getType().getName());
//                                        }
//
//                                        @Override
//                                        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
//                                            try {
//
//                                                return Optional.ofNullable(parameterMap.get(parameterContext.getParameter().getType().getName()))
//                                                        .map(fn -> fn.apply(extensionContext, context))
//                                                        .orElseThrow(() -> new IllegalArgumentException(
//                                                                "Unsupported parameter type: " + parameterContext.getParameter().getType()));
//                                            } catch (Exception e) {
//                                                throw new Error("Failed to resolve parameter: " + parameterContext.getParameter(), e);
//                                            }
//                                        }
//                                    });
//                        }
//                    });
//        }
//    }
//}
