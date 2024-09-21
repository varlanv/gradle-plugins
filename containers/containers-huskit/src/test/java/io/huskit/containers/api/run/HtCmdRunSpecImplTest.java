package io.huskit.containers.api.run;

import io.huskit.containers.api.HtDockerImageName;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtCmdRunSpecImplTest implements UnitTest {

    HtDockerImageName imageName = HtDockerImageName.of("anyImage");

    @Test
    void build__no_args__returns_default_command() {
        var subject = new HtCmdRunSpecImpl(imageName);

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                imageName.id()
        );
    }

    @Test
    void build__with_one_label__returns_command_with_label() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withLabels(Map.of("key", "value"));

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                "--label",
                "\"key=value\"",
                imageName.id()
        );
    }

    @Test
    void build__with_two_labels__returns_command_with_labels() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withLabels(Map.of("key1", "value1", "key2", "value2"));

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                "--label",
                "\"key1=value1\"",
                "--label",
                "\"key2=value2\"",
                imageName.id()
        );
    }

    @Test
    void build__with_remove__returns_command_with_remove() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withRemove();

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                "--rm",
                imageName.id()
        );
    }

    @Test
    void build__with_one_env__returns_command_with_env() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withEnv(Map.of("key", "value"));

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                "-e",
                "\"key=value\"",
                imageName.id()
        );
    }

    @Test
    void build__with_two_envs__returns_command_with_envs() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withEnv(Map.of("key1", "value1", "key2", "value2"));

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                "-e",
                "\"key1=value1\"",
                "-e",
                "\"key2=value2\"",
                imageName.id()
        );
    }

    @Test
    void build__with_command__returns_command_with_additional_command() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withCommand("command", "arg1", "arg2");

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                imageName.id(),
                "command",
                "arg1",
                "arg2"
        );
    }

    @Test
    void build__with_label_and_remove_and_command__returns_command_with_label_and_remove_and_additional_command() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withLabels(Map.of("key", "value"))
                .withRemove()
                .withCommand("command", "arg1", "arg2");

        var actual = subject.build();

        assertThat(actual).containsExactly(
                "docker",
                "run",
                "-d",
                "--rm",
                "--label",
                "\"key=value\"",
                imageName.id(),
                "command",
                "arg1",
                "arg2"
        );
    }
}
