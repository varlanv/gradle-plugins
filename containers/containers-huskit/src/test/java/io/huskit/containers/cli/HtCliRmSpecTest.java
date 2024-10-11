package io.huskit.containers.cli;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtCliRmSpecTest implements UnitTest {

    @Test
    void toCommand__no_args__should_throw_exception() {
        assertThatThrownBy(HtCliRmSpec::new)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Container IDs must not be empty");
    }

    @Test
    void build__with_ids_list__should_return_spec_with_ids() {
        var subject = new HtCliRmSpec(List.of("id1", "id2"));

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "rm", "id1", "id2");
    }

    @Test
    void toCommand__empty_ids_list__should_throw_exception() {
        assertThatThrownBy(() -> new HtCliRmSpec(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Container IDs must not be empty");
    }

    @Test
    void build__with_two_vararg_id__should_return_spec_with_ids() {
        var subject = new HtCliRmSpec("id1", "id2");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "rm", "id1", "id2");
    }

    @Test
    void build__with_one_vararg_id__should_return_spec_with_one_id() {
        var subject = new HtCliRmSpec("id1");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "rm", "id1");
    }

    @Test
    void build__with_force_true__should_return_spec_with_force() {
        var subject = new HtCliRmSpec("someid").withForce(true);

        var actual = subject.toCommand();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "--force", "someid"));
    }

    @Test
    void build__with_volumes_true__should_return_spec_with_volumes() {
        var subject = new HtCliRmSpec("someid").withVolumes(true);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "rm", "--volumes", "someid");
    }

    @Test
    void build__with_two_ids_and_force_and_volumes__should_return_spec_with_ids_force_and_volumes() {
        var subject = new HtCliRmSpec(List.of("id1", "id2")).withForce(true).withVolumes(true);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "rm", "--force", "--volumes", "id1", "id2");
    }
}
