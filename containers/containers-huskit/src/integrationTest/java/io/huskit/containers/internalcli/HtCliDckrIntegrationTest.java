package io.huskit.containers.internalcli;

import io.huskit.containers.api.*;
import io.huskit.containers.api.logs.LookFor;
import io.huskit.gradle.commontest.DockerAvailableCondition;
import io.huskit.gradle.commontest.DockerIntegrationTest;
import io.huskit.gradle.commontest.EnabledIfShellPresent;
import lombok.experimental.NonFinal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test suite using a shared {@link HtCliDocker} instance across most of the tests.
 *
 * <p>In some cases, this instance may be accessed concurrently by multiple tests.
 * While it's generally not recommended to share instances between tests due to potential
 * flakiness, {@link HtCliDocker} is designed to be thread-safe. Therefore, test flakiness
 * in this context can often indicate a bug in the implementation.</p>
 */
@ExtendWith(DockerAvailableCondition.class)
abstract class HtCliDckrIntegrationTest implements DockerIntegrationTest {

    private @NonFinal HtCliDocker subject;
    private @NonFinal ThreadLocalCliRecorder recorder;
    String helloWorldImage = "hello-world";

    abstract ShellType shellType();

    @EnabledIfShellPresent.Cmd
    static class CmdShellCliTest extends HtCliDckrIntegrationTest {

        @Override
        ShellType shellType() {
            return ShellType.CMD;
        }
    }

    @EnabledIfShellPresent.PowerShell
    static class PowerShellCliTest extends HtCliDckrIntegrationTest {

        @Override
        ShellType shellType() {
            return ShellType.POWERSHELL;
        }
    }

    @EnabledIfShellPresent.Bash
    static class BashCliTest extends HtCliDckrIntegrationTest {

        @Override
        ShellType shellType() {
            return ShellType.BASH;
        }
    }

    @EnabledIfShellPresent.Sh
    static class ShCliTest extends HtCliDckrIntegrationTest {

        @Override
        ShellType shellType() {
            return ShellType.SH;
        }
    }

    @BeforeAll
    void setupAll() {
        recorder = new ThreadLocalCliRecorder();
        subject = HtDocker.cli().configure(spec -> spec.withCliRecorder(recorder).withShell(shellType()));
    }

    @AfterAll
    void afterAll() {
        subject.close();
    }

    @BeforeEach
    void setup() {
        recorder.clearForCurrentThread();
    }

    @Nested
    class ContainersFixture {

        @Nested
        @DisplayName(".list()")
        class ListFixture {

            @Test
            @Execution(ExecutionMode.CONCURRENT)
            @DisplayName("withAll should create correct command")
            void list__with_all__ok() {
                // given
                var expectedFindIdsCommand = List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"", "--format", "\"{{.ID}}\"");
                var containers = subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withAll()
                                        .build())
                        .asList();

                assertThat(containers).isNotNull();
                assertThat(recorder.forCurrentThread().size()).isGreaterThanOrEqualTo(1);
                assertThat(recorder.forCurrentThread().get(0).value()).isEqualTo(expectedFindIdsCommand);
            }

            @Test
            @Execution(ExecutionMode.CONCURRENT)
            @DisplayName("withFilter by id should create correct command")
            void list__with_filter_by_id__ok() {
                var id = "SOME___Id__That__should_NOT__exist";
                var expectedFindIdsCommand = List.of("docker", "ps", "--format", "\"{{json .}}\"",
                        "--filter", "\"id=" + id + "\"", "--format", "\"{{.ID}}\"");
                var containers = subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withFilter(filterSpec -> filterSpec.id(id))
                                        .build())
                        .asList();

                assertThat(containers).isEmpty();
                assertThat(recorder.forCurrentThread()).hasSize(1);
                assertThat(recorder.forCurrentThread().get(0).value()).isEqualTo(expectedFindIdsCommand);
            }

            @Test
            @Execution(ExecutionMode.CONCURRENT)
            @DisplayName("withFilter and withAll should create correct command")
            void list__with_filter__and_all__ok() {
                var id = "SOME___Id__That__should_NOT__exist";
                var containers = subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withAll()
                                        .withFilter(filterSpec -> filterSpec.id(id))
                                        .build())
                        .asList();

                assertThat(containers).isNotNull();
                assertThat(recorder.forCurrentThread().size()).isGreaterThanOrEqualTo(1);
                assertThat(recorder.forCurrentThread().get(0).value()).isEqualTo(List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"",
                        "--filter", "\"id=" + id + "\"", "--format", "\"{{.ID}}\""));
            }
        }

        @Nested
        @DisplayName(".run()")
        class RunFixture {

            @Test
            @DisplayName("should create correct command")
            void run__should_create_correct_command() {
                // given
                var container = subject.containers().run(helloWorldImage).exec();

                // then
                try {
                    assertThat(recorder.forCurrentThread()).hasSize(1);
                    assertThat(recorder.forCurrentThread().get(0).value())
                            .containsExactly("docker", "run", "-d", helloWorldImage);
                } finally {
                    subject.containers().remove(container.id()).withForce().exec();
                }
            }

            @Test
            @DisplayName("when not called exec then should not run command")
            void run__when_not_called_exec__should_not_run_command() {
                // given
                subject.containers().run(helloWorldImage);

                // then
                assertThat(recorder.forCurrentThread()).isEmpty();
            }

            @Test
            @Execution(ExecutionMode.CONCURRENT)
            @DisplayName("with image passed as object should create correct command")
            void run__with_object_image__should_create_correct_command() {
                // given
                subject.containers().run(helloWorldImage, HtRunSpec::withRemove)
                        .exec();

                // then
                assertThat(recorder.forCurrentThread()).hasSize(1);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .containsExactly("docker", "run", "-d", "--rm", helloWorldImage);
            }

            @Test
            @Execution(ExecutionMode.CONCURRENT)
            @DisplayName("with labels should create correct command")
            void run__with_labels__should_create_correct_command() {
                // given
                var labels = new LinkedHashMap<String, String>();
                labels.put("someLabelKey", "someLabelVal");
                labels.put("someLabelKey2", "someLabelVal2");
                subject.containers().run(helloWorldImage, spec -> spec.withLabels(labels).withRemove())
                        .exec();

                // then
                assertThat(recorder.forCurrentThread()).hasSize(1);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .containsExactly("docker", "run", "-d", "--rm",
                                "--label", "\"someLabelKey=someLabelVal\"",
                                "--label", "\"someLabelKey2=someLabelVal2\"",
                                helloWorldImage);
            }
        }

        @Nested
        @DisplayName(".remove()")
        class RemoveFixture {

            @Test
            @DisplayName("should create correct command")
            void remove__should_create_correct_command() {
                // given
                var id = subject.containers().run(helloWorldImage).exec().id();

                // when
                subject.containers().remove(id).exec();

                // then
                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .containsExactly("docker", "run", "-d", helloWorldImage);
                assertThat(recorder.forCurrentThread().get(1).value())
                        .containsExactly("docker", "rm", id);
            }

            @Test
            @DisplayName("withForce should create correct command")
            void remove__withForce_should_create_correct_command() {
                // given
                var id = subject.containers().run(helloWorldImage).exec().id();

                // when
                subject.containers().remove(id).withForce().exec();

                // then
                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .containsExactly("docker", "run", "-d", helloWorldImage);
                assertThat(recorder.forCurrentThread().get(1).value())
                        .containsExactly("docker", "rm", "--force", id);
            }

            @Test
            @DisplayName("withVolumes should create correct command")
            void remove__withVolumes_should_create_correct_command() {
                // given
                var id = subject.containers().run(helloWorldImage).exec().id();

                // when
                subject.containers().remove(id).withVolumes().exec();

                // then
                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .containsExactly("docker", "run", "-d", helloWorldImage);
                assertThat(recorder.forCurrentThread().get(1).value())
                        .containsExactly("docker", "rm", "--volumes", id);
            }

            @Test
            @DisplayName("withForce and withVolumes should create correct command")
            void remove__withForce_withVolumes_should_create_correct_command() {
                // given
                var id = subject.containers().run(helloWorldImage).exec().id();

                // when
                subject.containers().remove(id).withForce().withVolumes().exec();

                // then
                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .containsExactly("docker", "run", "-d", helloWorldImage);
                assertThat(recorder.forCurrentThread().get(1).value())
                        .containsExactly("docker", "rm", "--force", "--volumes", id);
            }
        }

        @Nested
        @DisplayName("When one hello-world container is created")
        class OneContainerFixture {

            private @NonFinal String labelIdKey;
            private @NonFinal String labelId;
            private @NonFinal Map<String, String> labels;
            private @NonFinal String containerId;
            private @NonFinal String trimmedId;

            @BeforeAll
            void setupAll() {
                labelIdKey = "labelId";
                labelId = "some_very_unique_id_that_should_exist_only_in_this_test" + ThreadLocalRandom.current().nextDouble();
                labels = Map.of(
                        labelIdKey, labelId,
                        "someLabelKey1", "someLabelVal1",
                        "someLabelKey2", "someLabelVal2"
                );
                var container = subject.containers().run(
                        "alpine:3.20.3",
                        spec -> spec
                                .withCommand("sh -c \"echo 'Hello World 1' && echo 'Hello World 2' && tail -f /dev/null\"")
                                .withLabels(labels)
                ).exec();
                containerId = container.id();
                trimmedId = containerId.substring(0, 12);
                assertThat(container).isNotNull();
                assertThat(containerId).isNotBlank();
                assertThat(container.labels()).isEqualTo(labels);
                recorder.forCurrentThread().clear();
            }

            @AfterAll
            void cleanupAll() {
                subject.containers().remove(containerId).withForce().exec();
            }

            @Test
            @DisplayName("`list` when finds one container by id should return correct container")
            void list__when_finds_one_container_by_id__should_run_correct_commands() {
                subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withFilter(filterSpec -> filterSpec.id(containerId))
                                        .build())
                        .asList();

                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .isEqualTo(List.of("docker", "ps", "--format", "\"{{json .}}\"", "--filter", "\"id=" + containerId + "\"",
                                "--format", "\"{{.ID}}\""));
                assertThat(recorder.forCurrentThread().get(1).value())
                        .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", trimmedId));
            }

            @Test
            @DisplayName("`list` when finds one container by id should return correct container")
            void list__when_finds_one_container_by_id__should_return_correct_container() {
                var containers = subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withFilter(filterSpec -> filterSpec.id(containerId))
                                        .build())
                        .asList();

                assertThat(containers).hasSize(1);
                verifyContainer(containers);
            }

            @Test
            @DisplayName("`list` when finds one container by label should run correct commands")
            void list__when_finds_one_container_by_label__should_run_correct_commands() {
                subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withFilter(filterSpec -> filterSpec.label(labelIdKey, labelId))
                                        .build())
                        .asList();

                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .isEqualTo(List.of("docker", "ps", "--format", "\"{{json .}}\"", "--filter",
                                "\"label=" + labelIdKey + "=" + labelId + "\"", "--format", "\"{{.ID}}\""));
                assertThat(recorder.forCurrentThread().get(1).value())
                        .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", trimmedId));
            }

            @Test
            @DisplayName("`list` when finds one container by label should return correct container")
            void list__when_finds_one_container_by_label__should_return_correct_container() {
                var containers = subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withFilter(filterSpec -> filterSpec.label(labelIdKey, labelId))
                                        .build())
                        .asList();

                assertThat(containers).hasSize(1);
                verifyContainer(containers);
            }

            @Test
            @DisplayName("`list` with all and filter by label and filter by id should run correct commands")
            void list__with_all_and_filter_by_label_and_filter_by_id__should_run_correct_commands() {
                subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withAll()
                                        .withFilter(filterSpec -> filterSpec.label(labelIdKey, labelId))
                                        .withFilter(filterSpec -> filterSpec.id(containerId))
                                        .build())
                        .asList();

                assertThat(recorder.forCurrentThread()).hasSize(2);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .isEqualTo(List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"", "--filter",
                                "\"label=" + labelIdKey + "=" + labelId + "\"", "--filter",
                                "\"id=" + containerId + "\"", "--format", "\"{{.ID}}\""));
                assertThat(recorder.forCurrentThread().get(1).value())
                        .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", trimmedId));
            }

            @Test
            @DisplayName("`list` with all and filter by label and filter by id should return correct container")
            void list__with_all_and_filter_by_label_and_filter_by_id__should_return_correct_container() {
                var containers = subject.containers().list().withArgs(argsSpec ->
                                argsSpec.withAll()
                                        .withFilter(filterSpec -> filterSpec.label(labelIdKey, labelId))
                                        .withFilter(filterSpec -> filterSpec.id(containerId))
                                        .build())
                        .asList();

                assertThat(containers).hasSize(1);
                verifyContainer(containers);
            }

            @Test
            @DisplayName("`logs` should return log messages")
            void logs__should_return_log_messages() {
                var logs = subject.containers().logs(containerId);

                verifyLogMessages(logs);
            }

            @Test
            @DisplayName("`logs` should create correct command")
            void logs__should_create_correct_command() {
                subject.containers().logs(containerId)
                        .stream()
                        .collect(Collectors.toList());

                assertThat(recorder.forCurrentThread()).hasSize(1);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .isEqualTo(List.of("docker", "logs", containerId));
            }

            @Test
            @DisplayName("`logs` if didn't consume stream should not run command")
            void logs__if_didnt_consume_stream__should_not_run_command() {
                subject.containers().logs(containerId)
                        .stream()
                        .map(String::length)
                        .filter(i -> i > 0);

                assertThat(recorder.forCurrentThread()).isEmpty();
            }

            @RepeatedTest(2)
            @DisplayName("`logs` with follow and `lookFor` spec should return first log message")
            void logs__withFollow_lookFor__should_return_first_log_message() {
                var logs = subject.containers().logs(containerId).follow().lookFor(LookFor.word("Hello"));

                var logsList = logs.stream().collect(Collectors.toList());

                assertThat(logsList).containsExactly("Hello World 1");
            }

            @Test
            @DisplayName("`logs` with follow and `lookFor` spec should return all log messages up to first match")
            void logs__withFollow_lookFor__should_return_all_log_messages_up_to_first_match() {
                var logs = subject.containers().logs(containerId)
                        .follow()
                        .lookFor(LookFor.word("Hello World 2"));

                var logsList = logs.stream().collect(Collectors.toList());

                assertThat(logsList).containsExactly("Hello World 1", "Hello World 2");
            }

            @Test
            @DisplayName("`logs` with follow and `lookFor` spec should create correct command")
            void logs__withFollow_lookFor__should_create_correct_command() {
                subject.containers().logs(containerId)
                        .follow()
                        .lookFor(LookFor.word("Hello"))
                        .stream()
                        .collect(Collectors.toList());

                assertThat(recorder.forCurrentThread()).hasSize(1);
                assertThat(recorder.forCurrentThread().get(0).value())
                        .isEqualTo(List.of("docker", "logs", "-f", containerId));
            }

            @RepeatedTest(2)
            @DisplayName("`logs` with follow when can't find within timeout should throw exception")
            void logs__withFollow_lookFor_withTimout_cant_find__should_throw_exception() {
                assertThatThrownBy(() -> subject.containers().logs(containerId).follow()
                        .lookFor(LookFor.word("nonExistentWord").withTimeout(Duration.ofMillis(20)))
                        .stream()
                        .collect(Collectors.toList()))
                        .isInstanceOf(TimeoutException.class);
            }

            void verifyContainer(List<HtContainer> containers) {
                var actual = containers.get(0);
                assertThat(actual.id()).isEqualTo(containerId);
                assertThat(actual.labels()).isEqualTo(labels);
            }

            void verifyLogMessages(HtLogs logs) {
                var logsList = logs.stream().collect(Collectors.toList());

                assertThat(logsList).containsExactly("Hello World 1", "Hello World 2");
            }
        }
    }
}
