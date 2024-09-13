package io.huskit.containers.cli;

import io.huskit.containers.HtDefaultDockerImageName;
import io.huskit.containers.api.*;
import io.huskit.containers.api.logs.LookFor;
import io.huskit.containers.api.run.HtRunOptions;
import io.huskit.gradle.commontest.DockerAvailableCondition;
import io.huskit.gradle.commontest.IntegrationTest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@ExtendWith(DockerAvailableCondition.class)
public class HtCliDckrIntegrationTest implements IntegrationTest {

    String helloWorldImage = "hello-world";

    @AfterAll
    void afterAll() {
        CliFixture.sharedSubject.close();
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void list__with_all__ok() {
        runCliFixture(fixture -> {
            // given
            var expectedFindIdsCommand = List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"", "--format", "\"{{.ID}}\"");
            var containers = fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withAll()
                                    .build())
                    .asList();

            assertThat(containers).isNotNull();
            assertThat(fixture.commands().size()).isGreaterThanOrEqualTo(1);
            assertThat(fixture.commands().get(0).value()).isEqualTo(expectedFindIdsCommand);
        });
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void list__with_filter_by_id__ok() {
        runCliFixture(fixture -> {
            var id = "SOME___Id__That__should_NOT__exist";
            var expectedFindIdsCommand = List.of("docker", "ps", "--format", "\"{{json .}}\"",
                    "--filter", "\"id=" + id + "\"", "--format", "\"{{.ID}}\"");
            var containers = fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.id(id))
                                    .build())
                    .asList();

            assertThat(containers).isEmpty();
            assertThat(fixture.commands()).hasSize(1);
            assertThat(fixture.commands().get(0).value()).isEqualTo(expectedFindIdsCommand);
        });
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void list__with_filter__and_all__ok() {
        runCliFixture(fixture -> {
            var id = "SOME___Id__That__should_NOT__exist";
            var containers = fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withAll()
                                    .withFilter(filterSpec -> filterSpec.id(id))
                                    .build())
                    .asList();

            assertThat(containers).isNotNull();
            assertThat(fixture.commands().size()).isGreaterThanOrEqualTo(1);
            assertThat(fixture.commands().get(0).value()).isEqualTo(List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"",
                    "--filter", "\"id=" + id + "\"", "--format", "\"{{.ID}}\""));
        });
    }

    @Test
    void listContainers__when_finds_one_container_by_id__should_run_correct_commands() {
        runOneContainerFixture(fixture -> {
            fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.id(fixture.containerId()))
                                    .build())
                    .asList();

            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .isEqualTo(List.of("docker", "ps", "--format", "\"{{json .}}\"", "--filter", "\"id=" + fixture.containerId() + "\"",
                            "--format", "\"{{.ID}}\""));
            assertThat(fixture.commands().get(1).value())
                    .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", fixture.trimmedId()));
        });
    }

    @Test
    void listContainers__when_finds_one_container_by_id__should_return_correct_container() {
        runOneContainerFixture(fixture -> {
            var containers = fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.id(fixture.containerId()))
                                    .build())
                    .asList();

            assertThat(containers).hasSize(1);
            fixture.verifyContainer(containers);
        });
    }

    @Test
    void listContainers__when_finds_one_container_by_label__should_run_correct_commands() {
        runOneContainerFixture(fixture -> {
            fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.label(fixture.labelIdKey(), fixture.labelId()))
                                    .build())
                    .asList();

            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .isEqualTo(List.of("docker", "ps", "--format", "\"{{json .}}\"", "--filter",
                            "\"label=" + fixture.labelIdKey() + "=" + fixture.labelId() + "\"", "--format", "\"{{.ID}}\""));
            assertThat(fixture.commands().get(1).value())
                    .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", fixture.trimmedId()));
        });
    }

    @Test
    void listContainers__when_finds_one_container_by_label__should_return_correct_container() {
        runOneContainerFixture(fixture -> {
            var containers = fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.label(fixture.labelIdKey(), fixture.labelId()))
                                    .build())
                    .asList();

            assertThat(containers).hasSize(1);
            fixture.verifyContainer(containers);
        });
    }

    @Test
    void listContainers__with_all_and_filter_by_label_and_filter_by_id__should_run_correct_commands() {
        runOneContainerFixture(fixture -> {
            fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withAll()
                                    .withFilter(filterSpec -> filterSpec.label(fixture.labelIdKey(), fixture.labelId()))
                                    .withFilter(filterSpec -> filterSpec.id(fixture.containerId()))
                                    .build())
                    .asList();

            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .isEqualTo(List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"", "--filter",
                            "\"label=" + fixture.labelIdKey() + "=" + fixture.labelId() + "\"", "--filter",
                            "\"id=" + fixture.containerId() + "\"", "--format", "\"{{.ID}}\""));
            assertThat(fixture.commands().get(1).value())
                    .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", fixture.trimmedId()));
        });
    }

    @Test
    void listContainers__with_all_and_filter_by_label_and_filter_by_id__should_return_correct_container() {
        runOneContainerFixture(fixture -> {
            var containers = fixture.subject().listContainers().withArgs(argsSpec ->
                            argsSpec.withAll()
                                    .withFilter(filterSpec -> filterSpec.label(fixture.labelIdKey(), fixture.labelId()))
                                    .withFilter(filterSpec -> filterSpec.id(fixture.containerId()))
                                    .build())
                    .asList();

            assertThat(containers).hasSize(1);
            fixture.verifyContainer(containers);
        });
    }

    @Test
    void logs__should_return_log_messages() {
        runOneContainerFixture(fixture -> {
            var logs = fixture.subject().logs(fixture.containerId());

            fixture.verifyLogMessages(logs);
        });
    }

    @Test
    void logs__should_create_correct_command() {
        runOneContainerFixture(fixture -> {
            fixture.subject().logs(fixture.containerId())
                    .stream()
                    .collect(Collectors.toList());

            assertThat(fixture.commands()).hasSize(1);
            assertThat(fixture.commands().get(0).value())
                    .isEqualTo(List.of("docker", "logs", fixture.containerId()));
        });
    }

    @Test
    void logs__if_didnt_consume_stream__should_not_run_command() {
        runOneContainerFixture(fixture -> {
            fixture.subject().logs(fixture.containerId())
                    .stream()
                    .map(String::length)
                    .filter(i -> i > 0);

            assertThat(fixture.commands()).isEmpty();
        });
    }

    @RepeatedTest(2)
    void logs__withFollow_lookFor__should_return_first_log_message() {
        runOneContainerFixture(fixture -> {
            var logs = fixture.subject().logs(fixture.containerId()).follow().lookFor(LookFor.word("Hello"));

            var logsList = logs.stream().collect(Collectors.toList());

            assertThat(logsList).containsExactly("Hello World 1");
        });
    }

    @Test
    void logs__withFollow_lookFor__should_return_all_log_messages_up_to_first_match() {
        runOneContainerFixture(fixture -> {
            var logs = fixture.subject().logs(fixture.containerId())
                    .follow()
                    .lookFor(LookFor.word("Hello World 2"));

            var logsList = logs.stream().collect(Collectors.toList());

            assertThat(logsList).containsExactly("Hello World 1", "Hello World 2");
        });
    }

    @Test
    void logs__withFollow_lookFor__should_create_correct_command() {
        runOneContainerFixture(fixture -> {
            fixture.subject().logs(fixture.containerId())
                    .follow()
                    .lookFor(LookFor.word("Hello"))
                    .stream()
                    .collect(Collectors.toList());

            assertThat(fixture.commands()).hasSize(1);
            assertThat(fixture.commands().get(0).value())
                    .isEqualTo(List.of("docker", "logs", "-f", fixture.containerId()));
        });
    }

    @Test
    void run__should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            var container = fixture.subject().run(helloWorldImage).exec();

            // then
            try {
                assertThat(fixture.commands()).hasSize(1);
                assertThat(fixture.commands().get(0).value())
                        .containsExactly("docker", "run", "-d", helloWorldImage);
            } finally {
                fixture.subject().remove(container.id()).withForce().exec();
            }
        });
    }

    @Test
    void run__when_not_called_exec__should_not_run_command() {
        runCliFixture(fixture -> {
            // given
            fixture.subject().run(helloWorldImage);

            // then
            assertThat(fixture.commands()).isEmpty();
        });
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void run__with_object_image__should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            fixture.subject().run(new HtDefaultDockerImageName(helloWorldImage))
                    .withOptions(HtRunOptions::withRemove)
                    .exec();

            // then
            assertThat(fixture.commands()).hasSize(1);
            assertThat(fixture.commands().get(0).value())
                    .containsExactly("docker", "run", "-d", "--rm", helloWorldImage);
        });
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    void run__with_labels__should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            var labels = new LinkedHashMap<String, String>();
            labels.put("someLabelKey", "someLabelVal");
            labels.put("someLabelKey2", "someLabelVal2");
            fixture.subject().run(new HtDefaultDockerImageName(helloWorldImage))
                    .withOptions(htRunOptions -> htRunOptions.withRemove()
                            .withLabels(labels))
                    .exec();

            // then
            assertThat(fixture.commands()).hasSize(1);
            assertThat(fixture.commands().get(0).value())
                    .containsExactly("docker", "run", "-d", "--rm",
                            "--label", "\"someLabelKey=someLabelVal\"",
                            "--label", "\"someLabelKey2=someLabelVal2\"",
                            helloWorldImage);
        });
    }

    @Test
    void remove__should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            var id = fixture.subject().run(helloWorldImage).exec().id();

            // when
            fixture.subject().remove(id).exec();

            // then
            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .containsExactly("docker", "run", "-d", helloWorldImage);
            assertThat(fixture.commands().get(1).value())
                    .containsExactly("docker", "rm", id);
        });
    }

    @Test
    void remove__withForce_should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            var id = fixture.subject().run(helloWorldImage).exec().id();

            // when
            fixture.subject().remove(id).withForce().exec();

            // then
            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .containsExactly("docker", "run", "-d", helloWorldImage);
            assertThat(fixture.commands().get(1).value())
                    .containsExactly("docker", "rm", "--force", id);
        });
    }

    @Test
    void remove__withVolumes_should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            var id = fixture.subject().run(helloWorldImage).exec().id();

            // when
            fixture.subject().remove(id).withVolumes().exec();

            // then
            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .containsExactly("docker", "run", "-d", helloWorldImage);
            assertThat(fixture.commands().get(1).value())
                    .containsExactly("docker", "rm", "--volumes", id);
        });
    }

    @Test
    void remove__withForce_withVolumes_should_create_correct_command() {
        runCliFixture(fixture -> {
            // given
            var id = fixture.subject().run(helloWorldImage).exec().id();

            // when
            fixture.subject().remove(id).withForce().withVolumes().exec();

            // then
            assertThat(fixture.commands()).hasSize(2);
            assertThat(fixture.commands().get(0).value())
                    .containsExactly("docker", "run", "-d", helloWorldImage);
            assertThat(fixture.commands().get(1).value())
                    .containsExactly("docker", "rm", "--force", "--volumes", id);
        });
    }

    @RepeatedTest(2)
    void logs__withFollow_lookFor_withTimout_cant_find__should_throw_exception() {
        runOneContainerFixture(fixture -> {
            assertThatThrownBy(() -> fixture.subject().logs(fixture.containerId()).follow()
                    .lookFor(LookFor.word("nonExistentWord").withTimeout(Duration.ofMillis(20)))
                    .stream()
                    .collect(Collectors.toList()))
                    .isInstanceOf(TimeoutException.class);
        });
    }

//    @Disabled
//    @RepeatedTest(1)
//    void tst_github() {
//        DockerClient dockerClient = DockerClientFactory.instance().client();
//        var labels = Map.of("someLabelKey", "someLabelVal");
//        var container = dockerClient.createContainerCmd(helloWorldImage)
//                .withLabels(labels)
//                .exec();
//        dockerClient.startContainerCmd(container.getId()).exec();
//        var inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();
//
//        try {
//            assertThat(container).isNotNull();
//            assertThat(container.getId()).isNotBlank();
//            assertThat(inspectContainerResponse.getConfig().getLabels()).isEqualTo(labels);
//        } finally {
//            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
//        }
//    }

    @SneakyThrows
    private void runCliFixture(ThrowingConsumer<CliFixture> fixtureConsumer) {
        var commands = new ArrayList<HtCommand>();
        fixtureConsumer.accept(
                new CliFixture(
                        CliFixture.sharedSubject.configure(spec -> spec.withCliRecorder(CliRecorder.collection(commands))),
                        commands
                )
        );
    }

    @SneakyThrows
    private void runOneContainerFixture(ThrowingConsumer<OneContainerFixture> fixtureConsumer) {
        var commands = new ArrayList<HtCommand>();
        var fixture = new OneContainerFixture(
                CliFixture.sharedSubject.configure(spec -> spec.withCliRecorder(CliRecorder.collection(commands))),
                commands
        );
        fixtureConsumer.accept(fixture);
    }

    @Getter
    @RequiredArgsConstructor
    private static class CliFixture {

        private static final HtCliDocker sharedSubject = HtDocker.cli();
        HtCliDocker subject;
        List<HtCommand> commands;
    }

    @SuppressWarnings("all")
    private static class OneContainerFixture {

        private List<HtCommand> commands;
        private HtCliDocker subject;
        private static String labelIdKey;
        private static String labelId;
        private static Map<String, String> labels;
        private static String containerId;
        private static String trimmedId;
        private static HtContainer container;
        private static boolean initialized;

        public OneContainerFixture(HtCliDocker subject, List<HtCommand> commands) {
            this.commands = commands;
            this.subject = subject;
            if (!initialized) {
                labelIdKey = "labelId";
                labelId = "some_very_unique_id_that_should_exist_only_in_this_test" + ThreadLocalRandom.current().nextDouble();
                labels = Map.of(
                        labelIdKey, labelId,
                        "someLabelKey1", "someLabelVal1",
                        "someLabelKey2", "someLabelVal2"
                );
                container = subject.run("alpine:3.20.3")
                        .withOptions(options ->
                                options.withLabels(labels)
                                        .withCommand("sh -c \"echo 'Hello World 1' && echo 'Hello World 2' && tail -f /dev/null\""))
                        .exec();
                containerId = container.id();
                trimmedId = containerId.substring(0, 12);
                assertThat(container).isNotNull();
                assertThat(containerId).isNotBlank();
                assertThat(container.labels()).isEqualTo(labels);
                commands().clear();
            }
            initialized = true;
        }

        public HtCliDocker subject() {
            return subject;
        }

        public List<HtCommand> commands() {
            return commands;
        }

        public String labelIdKey() {
            return labelIdKey;
        }

        public String labelId() {
            return labelId;
        }

        public Map<String, String> labels() {
            return labels;
        }

        public String containerId() {
            return containerId;
        }

        public String trimmedId() {
            return trimmedId;
        }

        public HtContainer container() {
            return container;
        }

        public void verifyContainer(List<HtContainer> containers) {
            var actual = containers.get(0);
            assertThat(actual.id()).isEqualTo(containerId());
            assertThat(actual.labels()).isEqualTo(labels());
        }

        public void verifyLogMessages(HtLogs logs) {
            var logsList = logs.stream().collect(Collectors.toList());

            assertThat(logsList).containsExactly("Hello World 1", "Hello World 2");
        }
    }
}
