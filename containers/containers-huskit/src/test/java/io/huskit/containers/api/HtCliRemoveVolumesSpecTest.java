package io.huskit.containers.api;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtCliRemoveVolumesSpecTest implements UnitTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void toCommand__when_empty_volumeIds__then_throw_exception(Boolean force) {
        var subject = new HtCliRemoveVolumesSpec(force, List.of());

        assertThatThrownBy(subject::toCommand)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Received empty volume ID list");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void toCommand__when_blank_volumeId__then_throw_exception(Boolean force) {
        var subject = new HtCliRemoveVolumesSpec(force, List.of("some_id", "  "));

        assertThatThrownBy(subject::toCommand)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Volume ID cannot be blank");
    }

    @Test
    void toCommand__withForce__add_force_flag() {
        var subject = new HtCliRemoveVolumesSpec(true, List.of("some_id"));

        var result = subject.toCommand();

        assertThat(result)
                .containsExactly("docker", "volume", "rm", "--force", "some_id");
    }

    @Test
    void toCommand__withoutForce__do_not_add_force_flag() {
        var subject = new HtCliRemoveVolumesSpec(false, List.of("some_id"));

        var result = subject.toCommand();

        assertThat(result)
                .containsExactly("docker", "volume", "rm", "some_id");
    }

    @Test
    void toCommand__3_ids__return_command_with_3_ids() {
        var subject = new HtCliRemoveVolumesSpec(false, List.of("id1", "id2", "id3"));

        var result = subject.toCommand();

        assertThat(result)
                .containsExactly("docker", "volume", "rm", "id1", "id2", "id3");
    }

    @Test
    void toCommand__3_ids_with_force__return_command_with_3_ids_and_force() {
        var subject = new HtCliRemoveVolumesSpec(true, List.of("id1", "id2", "id3"));

        var result = subject.toCommand();

        assertThat(result)
                .containsExactly("docker", "volume", "rm", "--force", "id1", "id2", "id3");
    }
}
