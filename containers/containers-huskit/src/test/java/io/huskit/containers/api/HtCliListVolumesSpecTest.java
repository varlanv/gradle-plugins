package io.huskit.containers.api;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HtCliListVolumesSpecTest implements UnitTest {

    @Test
    void toCommand__no_filters__should_build_correct_command() {
        var subject = new HtCliListVolumesSpec();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "ls", "-q");
    }

    @Test
    void withFilterByDangling__true__should_build_correct_command() {
        var subject = new HtCliListVolumesSpec();

        subject.withFilterByDangling(true);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "ls", "-q", "--filter", "\"dangling=true\"");
    }

    @Test
    void withFilterByLabelExists__label_key__should_build_correct_command() {
        var subject = new HtCliListVolumesSpec();

        subject.withFilterByLabelExists("label_key");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "ls", "-q", "--filter", "\"label=label_key\"");
    }

    @Test
    void withFilterByLabel__label_key_label_value__should_build_correct_command() {
        var subject = new HtCliListVolumesSpec();

        subject.withFilterByLabel("label_key", "label_value");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "ls", "-q", "--filter", "\"label=label_key=label_value\"");
    }

    @Test
    void withFilterByLabels__labels__should_build_correct_command() {
        var subject = new HtCliListVolumesSpec();

        subject.withFilterByLabels(Map.of("label_key", "label_value"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "ls", "-q", "--filter", "\"label=label_key=label_value\"");
    }
}
