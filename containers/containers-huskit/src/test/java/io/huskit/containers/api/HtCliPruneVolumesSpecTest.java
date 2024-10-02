package io.huskit.containers.api;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtCliPruneVolumesSpecTest implements UnitTest {

    @Test
    void toCommand__withoutArgs__returnsDefaultCommand() {
        var subject = new HtCliPruneVolumesSpec();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f");
    }

    @Test
    void toCommand__withAll__returnsCommandWithAll() {
        var subject = new HtCliPruneVolumesSpec().withAll();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--all");
    }

    @Test
    void toCommand__withFilterByDangling__returnsCommandWithFilterByDangling() {
        var subject = new HtCliPruneVolumesSpec().withFilterByDangling(true);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--filter", "\"dangling=true\"");
    }

    @Test
    void toCommand___withAll_and_withFilterByDangling__returnsCommandWithAllAndFilterByDangling() {
        var subject = new HtCliPruneVolumesSpec()
                .withAll()
                .withFilterByDangling(true);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--all", "--filter", "\"dangling=true\"");
    }

    @Test
    void toCommand__withFilterByLabelExists__returnsCommandWithFilterByLabelExists() {
        var subject = new HtCliPruneVolumesSpec().withFilterByLabelExists("label");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--filter", "\"label=label\"");
    }

    @Test
    void toCommand__withFilterByLabelExists__when_called_twice__returnsCommandWithFilterByLabelExists() {
        var subject = new HtCliPruneVolumesSpec()
                .withFilterByLabelExists("label1")
                .withFilterByLabelExists("label2");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--filter", "\"label=label1\"", "--filter", "\"label=label2\"");
    }

    @Test
    void toCommand__withFilterByLabel__returnsCommandWithFilterByLabel() {
        var subject = new HtCliPruneVolumesSpec().withFilterByLabel("label", "value");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--filter", "\"label=label=value\"");
    }

    @Test
    void toCommand__withFilterByLabels__returnsCommandWithFilterByLabels() {
        var subject = new HtCliPruneVolumesSpec().withFilterByLabels(Map.of("key", "value"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "prune", "-f", "--filter", "\"label=key=value\"");
    }

    @Test
    void toCommand__withFilterByLabels__withThreeLabels__returnsCommandWithFilterByLabels() {
        var labels = new LinkedHashMap<String, String>();
        labels.put("key1", "value1");
        labels.put("key2", "value2");
        labels.put("key3", "value3");
        var subject = new HtCliPruneVolumesSpec().withFilterByLabels(labels);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "volume", "prune",
                "-f",
                "--filter", "\"label=key1=value1\"",
                "--filter", "\"label=key2=value2\"",
                "--filter", "\"label=key3=value3\""
        );
    }

    @Test
    void withFilterByLabels__nullValue__throwsException() {
        var labels = new HashMap<String, String>();
        labels.put("key1", "value");
        labels.put("key2", null);
        var subject = new HtCliPruneVolumesSpec();

        assertThatThrownBy(() -> subject.withFilterByLabels(labels))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Null label values are not allowed");
    }
}
