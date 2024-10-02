package io.huskit.containers.api;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class HtCliCreateVolumeSpecTest implements UnitTest {

    String volumeId = "volumeId";

    @Test
    void toCommand__empty__returns_default_command() {
        var subject = new HtCliCreateVolumeSpec();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "create");
    }

    @Test
    void toCommand__with_driver__returns_command_with_driver() {
        var subject = new HtCliCreateVolumeSpec(volumeId)
                .withDriver("driver");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "create", "--driver", "driver", volumeId);
    }

    @Test
    void toCommand__with_driver_opts__returns_command_with_driver_opts() {
        var subject = new HtCliCreateVolumeSpec(volumeId)
                .withDriverOpts("key", "value");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "create", "--opt", "\"key=value\"", volumeId);
    }

    @Test
    void toCommand__with_label_key__returns_command_with_label() {
        var subject = new HtCliCreateVolumeSpec(volumeId)
                .withLabel("label");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "create", "--label", "label", volumeId);
    }

    @Test
    void toCommand__with_label_pair__returns_command_with_label() {
        var subject = new HtCliCreateVolumeSpec(volumeId)
                .withLabel("labelKey", "labelValue");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "create", "--label", "\"labelKey=labelValue\"", volumeId);
    }

    @Test
    void toCommand__with_labels__returns_command_with_labels() {
        var labels = new LinkedHashMap<String, String>();
        labels.put("labelKey1", "labelValue1");
        labels.put("labelKey2", "labelValue2");
        var subject = new HtCliCreateVolumeSpec(volumeId).withLabels(labels);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "volume", "create",
                "--label", "\"labelKey1=labelValue1\"",
                "--label", "\"labelKey2=labelValue2\"",
                volumeId
        );
    }

    @Test
    void toCommand_withAllOptions__returns_command_with_all_options() {
        var labels = new LinkedHashMap<String, String>();
        labels.put("labelKey1", "labelValue1");
        labels.put("labelKey2", "labelValue2");
        var subject = new HtCliCreateVolumeSpec(volumeId)
                .withDriver("driver")
                .withDriverOpts("key", "value")
                .withLabel("label")
                .withLabel("labelKey", "labelValue")
                .withLabels(labels);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
                "docker", "volume", "create",
                "--driver", "driver",
                "--opt", "\"key=value\"",
                "--label", "label",
                "--label", "\"labelKey=labelValue\"",
                "--label", "\"labelKey1=labelValue1\"",
                "--label", "\"labelKey2=labelValue2\"",
                volumeId
        );
    }
}
