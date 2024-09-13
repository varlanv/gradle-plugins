package io.huskit.containers.cli;

import com.github.dockerjava.api.DockerClient;
import io.huskit.containers.api.*;
import io.huskit.containers.api.logs.LookFor;
import io.huskit.gradle.commontest.DockerAvailableCondition;
import io.huskit.gradle.commontest.IntegrationTest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.DockerClientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ExtendWith(DockerAvailableCondition.class)
public class HtCliDckrIntegrationTest implements IntegrationTest {

    List<HtCommand> commands = new ArrayList<>();
    HtCliDocker subject = HtDocker.cli()
            .configure(spec ->
                    spec.withCliRecorder(CliRecorder.collection(commands)));
    String helloWorldImage = "hello-world";

    @AfterEach
    void afterEach() {
        commands.clear();
    }

    @AfterAll
    void afterAll() {
        if (OneContainerFixture.initialized) {
            OneContainerFixture.close();
        }
        subject.close();
    }

    @Test
    void list__with_all__ok() {
        // given
        var expectedFindIdsCommand = List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"", "--format", "\"{{.ID}}\"");
        var containers = subject.listContainers().withArgs(argsSpec ->
                        argsSpec.withAll()
                                .build())
                .asList();

        assertThat(containers).isNotNull();
        assertThat(commands.size()).isGreaterThanOrEqualTo(1);
        assertThat(commands.get(0).value()).isEqualTo(expectedFindIdsCommand);
    }

    @Test
    void list__with_filter_by_id__ok() {
        var id = "SOME___Id__That__should_NOT__exist";
        var expectedFindIdsCommand = List.of("docker", "ps", "--format", "\"{{json .}}\"",
                "--filter", "\"id=" + id + "\"", "--format", "\"{{.ID}}\"");
        var containers = subject.listContainers().withArgs(argsSpec ->
                        argsSpec.withFilter(filterSpec -> filterSpec.id(id))
                                .build())
                .asList();

        assertThat(containers).isEmpty();
        assertThat(commands).hasSize(1);
        assertThat(commands.get(0).value()).isEqualTo(expectedFindIdsCommand);
    }

    @Test
    void list__with_filter__and_all__ok() {
        var id = "SOME___Id__That__should_NOT__exist";
        var containers = subject.listContainers().withArgs(argsSpec ->
                        argsSpec.withAll()
                                .withFilter(filterSpec -> filterSpec.id(id))
                                .build())
                .asList();

        assertThat(containers).isNotNull();
        assertThat(commands.size()).isGreaterThanOrEqualTo(1);
        assertThat(commands.get(0).value()).isEqualTo(List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"",
                "--filter", "\"id=" + id + "\"", "--format", "\"{{.ID}}\""));
    }

    @Test
    void listContainers__when_finds_one_container_by_id__should_run_correct_commands() {
        runOneContainerFixture(fixture -> {
            subject.listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.id(fixture.containerId()))
                                    .build())
                    .asList();

            assertThat(commands).hasSize(2);
            assertThat(commands.get(0).value())
                    .isEqualTo(List.of("docker", "ps", "--format", "\"{{json .}}\"", "--filter", "\"id=" + fixture.containerId() + "\"",
                            "--format", "\"{{.ID}}\""));
            assertThat(commands.get(1).value())
                    .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", fixture.trimmedId()));
        });
    }

    @Test
    void listContainers__when_finds_one_container_by_id__should_return_correct_container() {
        runOneContainerFixture(fixture -> {
            var containers = subject.listContainers().withArgs(argsSpec ->
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
            subject.listContainers().withArgs(argsSpec ->
                            argsSpec.withFilter(filterSpec -> filterSpec.label(fixture.labelIdKey(), fixture.labelId()))
                                    .build())
                    .asList();

            assertThat(commands).hasSize(2);
            assertThat(commands.get(0).value())
                    .isEqualTo(List.of("docker", "ps", "--format", "\"{{json .}}\"", "--filter",
                            "\"label=" + fixture.labelIdKey() + "=" + fixture.labelId() + "\"", "--format", "\"{{.ID}}\""));
            assertThat(commands.get(1).value())
                    .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", fixture.trimmedId()));
        });
    }

    @Test
    void listContainers__when_finds_one_container_by_label__should_return_correct_container() {
        runOneContainerFixture(fixture -> {
            var containers = subject.listContainers().withArgs(argsSpec ->
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
            subject.listContainers().withArgs(argsSpec ->
                            argsSpec.withAll()
                                    .withFilter(filterSpec -> filterSpec.label(fixture.labelIdKey(), fixture.labelId()))
                                    .withFilter(filterSpec -> filterSpec.id(fixture.containerId()))
                                    .build())
                    .asList();

            assertThat(commands).hasSize(2);
            assertThat(commands.get(0).value())
                    .isEqualTo(List.of("docker", "ps", "-a", "--format", "\"{{json .}}\"", "--filter",
                            "\"label=" + fixture.labelIdKey() + "=" + fixture.labelId() + "\"", "--filter",
                            "\"id=" + fixture.containerId() + "\"", "--format", "\"{{.ID}}\""));
            assertThat(commands.get(1).value())
                    .isEqualTo(List.of("docker", "inspect", "--format", "\"{{json .}}\"", fixture.trimmedId()));
        });
    }

    @Test
    void listContainers__with_all_and_filter_by_label_and_filter_by_id__should_return_correct_container() {
        runOneContainerFixture(fixture -> {
            var containers = subject.listContainers().withArgs(argsSpec ->
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
            var logs = subject.logs(fixture.containerId());

            fixture.verifyLogMessages(logs);
        });
    }

    @Test
    void logs__withFollow_lookFor__should_return_first_log_message() {
        runOneContainerFixture(fixture -> {
            var logs = subject.logs(fixture.containerId()).follow().lookFor(LookFor.word("Hello"));

            var logsList = logs.stream().collect(Collectors.toList());

            assertThat(logsList).containsExactly("Hello World 1");
        });
    }

    @Disabled
    @RepeatedTest(1)
    void tst_github() {
        DockerClient dockerClient = DockerClientFactory.instance().client();
        var labels = Map.of("someLabelKey", "someLabelVal");
        var container = dockerClient.createContainerCmd(helloWorldImage)
                .withLabels(labels)
                .exec();
        dockerClient.startContainerCmd(container.getId()).exec();
        var inspectContainerResponse = dockerClient.inspectContainerCmd(container.getId()).exec();

        try {
            assertThat(container).isNotNull();
            assertThat(container.getId()).isNotBlank();
            assertThat(inspectContainerResponse.getConfig().getLabels()).isEqualTo(labels);
        } finally {
            dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
        }
    }

    @SneakyThrows
    private void runOneContainerFixture(ThrowingConsumer<OneContainerFixture> fixtureConsumer) {
        var fixture = new OneContainerFixture(subject, commands);
        fixtureConsumer.accept(fixture);
    }

    @SuppressWarnings("all")
    private static class OneContainerFixture {

        private static HtCliDocker subject;
        private static String labelIdKey;
        private static String labelId;
        private static Map<String, String> labels;
        private static String containerId;
        private static String trimmedId;
        private static HtContainer container;
        private static boolean initialized;

        public OneContainerFixture(HtCliDocker subject, List<HtCommand> commands) {
            if (!initialized) {
                this.subject = subject;
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
                commands.clear();
            }
            initialized = true;
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

        static void close() {
            subject.remove(containerId).withForce().exec();
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
