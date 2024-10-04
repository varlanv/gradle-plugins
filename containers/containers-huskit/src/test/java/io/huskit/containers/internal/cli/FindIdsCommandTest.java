package io.huskit.containers.internal.cli;

import io.huskit.common.HtStrings;
import io.huskit.containers.api.cli.HtArg;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FindIdsCommandTest implements UnitTest {

    @Test
    void list__no_args__should_return_default_command() {
        var actual = new FindIdsCommand(List.of()).list();

        assertThat(actual).isNotEmpty();
        assertThat(actual).isEqualTo(List.of(
                "docker",
                "ps",
                "--format",
                HtStrings.doubleQuote("{{.ID}}")
        ));
    }

    @Test
    void list__one_arg__should_return_command_with_one_arg() {
        var actual = new FindIdsCommand(
                List.of(HtArg.of("argName", "argValue"))
        ).list();

        assertThat(actual).isNotEmpty();
        assertThat(actual).isEqualTo(List.of(
                "docker",
                "ps",
                "argName",
                "argValue",
                "--format",
                HtStrings.doubleQuote("{{.ID}}")
        ));
    }
}
