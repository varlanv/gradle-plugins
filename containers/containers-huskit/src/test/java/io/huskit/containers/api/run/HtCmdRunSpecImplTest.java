package io.huskit.containers.api.run;

import io.huskit.common.HtStrings;
import io.huskit.containers.api.HtImgName;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtCmdRunSpecImplTest implements UnitTest {

    HtImgName imageName = HtImgName.of("any:image");

    @Test
    void toCommand__no_args__returns_default_command() {
        var subject = new HtCmdRunSpecImpl(imageName);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d",
                imageName.reference()
        );
    }

    @Test
    void toCommand__with_one_label__returns_command_with_label() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withLabels(Map.of("key", "value"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d",
                "--label", HtStrings.doubleQuote("key=value"),
                imageName.reference()
        );
    }

    @Test
    void toCommand__with_two_labels__returns_command_with_labels() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withLabels(Map.of("key1", "value1", "key2", "value2"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d",
                "--label", HtStrings.doubleQuote("key1=value1"),
                "--label", HtStrings.doubleQuote("key2=value2"),
                imageName.reference()
        );
    }

    @Test
    void toCommand__with_remove__returns_command_with_remove() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withRemove();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d", "--rm",
                imageName.reference()
        );
    }

    @Test
    void toCommand__with_one_env__returns_command_with_env() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withEnv(Map.of("key", "value"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d",
                "-e", HtStrings.doubleQuote("key=value"),
                imageName.reference()
        );
    }

    @Test
    void toCommand__with_two_envs__returns_command_with_envs() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withEnv(Map.of("key1", "value1", "key2", "value2"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d",
                "-e", HtStrings.doubleQuote("key1=value1"),
                "-e", HtStrings.doubleQuote("key2=value2"),
                imageName.reference()
        );
    }

    @Test
    void toCommand__with_command__returns_command_with_additional_command() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withCommand("command", "arg1", "arg2");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d",
                imageName.reference(),
                "command", HtStrings.doubleQuote("arg1"), HtStrings.doubleQuote("arg2")
        );
    }

    @Test
    void toCommand__with_label_and_remove_and_command__returns_command_with_label_and_remove_and_additional_command() {
        var subject = new HtCmdRunSpecImpl(imageName)
                .withLabels(Map.of("key", "value"))
                .withRemove()
                .withCommand("command", "arg1", "arg2");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "run", "-d", "--rm",
                "--label", HtStrings.doubleQuote("key=value"),
                imageName.reference(),
                "command", HtStrings.doubleQuote("arg1"), HtStrings.doubleQuote("arg2")
        );
    }
}
