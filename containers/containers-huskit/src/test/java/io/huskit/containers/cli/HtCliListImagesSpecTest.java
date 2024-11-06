package io.huskit.containers.cli;

import io.huskit.common.HtStrings;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtCliListImagesSpecTest implements UnitTest {

    @Test
    void toCommand__withoutArgs__returnsDefaultCommand() {
        var subject = new HtCliListImagesSpec();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q");
    }

    @Test
    void toCommand__withAll__returnsCommandWithAll() {
        var subject = new HtCliListImagesSpec().withAll();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--all");
    }

    @Test
    void toCommand_withAllCalledTwice__returnsCommandWithAll() {
        var subject = new HtCliListImagesSpec().withAll().withAll();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--all");
    }

    @Test
    void toCommand__withFilterByBefore__returnsCommandWithFilterByBefore() {
        var subject = new HtCliListImagesSpec().withFilterByBefore("image");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("before=image"));
    }

    @Test
    void toCommand__withFilterBySince__returnsCommandWithFilterBySince() {
        var subject = new HtCliListImagesSpec().withFilterBySince("image");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("since=image"));
    }

    @Test
    void toCommand__withFilterByReference__returnsCommandWithFilterByReference() {
        var subject = new HtCliListImagesSpec().withFilterByReference("ref");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("reference=ref"));
    }

    @Test
    void toCommand__withFilterByUntil__returnsCommandWithFilterByUntil() {
        var subject = new HtCliListImagesSpec().withFilterByUntil("image");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("until=image"));
    }

    @Test
    void toCommand__withFilterByDangling__returnsCommandWithFilterByDangling() {
        var subject = new HtCliListImagesSpec().withFilterByDangling(true);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("dangling=true"));
    }

    @Test
    void toCommand__withFilterByLabel__returnsCommandWithFilterByLabel() {
        var subject = new HtCliListImagesSpec().withFilterByLabel("key");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("label=key"));
    }

    @Test
    void toCommand__withFilterByLabel__returnsCommandWithFilterByLabelAndValue() {
        var subject = new HtCliListImagesSpec().withFilterByLabel("key", "value");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "images", "-q", "--filter", HtStrings.doubleQuote("label=key=value"));
    }

    @Test
    void toCommand__all_args__returnsCommandWithAllArgs() {
        var subject = new HtCliListImagesSpec()
            .withAll()
            .withFilterByBefore("before")
            .withFilterBySince("since")
            .withFilterByReference("reference")
            .withFilterByUntil("until")
            .withFilterByDangling(true)
            .withFilterByLabel("key")
            .withFilterByLabel("key", "value");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
            "docker", "images",
            "-q",
            "--all",
            "--filter", HtStrings.doubleQuote("before=before"),
            "--filter", HtStrings.doubleQuote("since=since"),
            "--filter", HtStrings.doubleQuote("reference=reference"),
            "--filter", HtStrings.doubleQuote("until=until"),
            "--filter", HtStrings.doubleQuote("dangling=true"),
            "--filter", HtStrings.doubleQuote("label=key"),
            "--filter", HtStrings.doubleQuote("label=key=value")
        );
    }
}
