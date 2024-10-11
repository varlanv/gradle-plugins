package io.huskit.containers.cli;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtCliInspectVolumesSpecTest implements UnitTest {

    @Test
    void toCommand__null_volume_id__throws_exception() {
        assertThatThrownBy(() -> new HtCliInspectVolumesSpec((CharSequence) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toCommand__blank_volume_id__throws_exception() {
        assertThatThrownBy(() -> new HtCliInspectVolumesSpec(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Volume ID cannot be blank");
    }

    @Test
    void toCommand__one_id__should_build_correct_command() {
        var volumeId = "volumeid";
        var subject = new HtCliInspectVolumesSpec(volumeId);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "inspect", "--format=\"{{json .}}\"", volumeId);
    }

    @Test
    void toCommand__multiple_ids__should_build_correct_command() {
        var volumeId1 = "volumeid1";
        var volumeId2 = "volumeid2";
        var subject = new HtCliInspectVolumesSpec(List.of(volumeId1, volumeId2));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "volume", "inspect", "--format=\"{{json .}}\"", volumeId1, volumeId2);
    }
}
