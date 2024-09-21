package io.huskit.containers.internal.cli;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtCliRmSpecTest implements UnitTest {

    @Test
    void build_not_args__should_return_default_spec() {
        var subject = new HtCliRmSpec();

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm"));
    }

    @Test
    void build__with_ids_list__should_return_spec_with_ids() {
        var subject = new HtCliRmSpec(List.of("id1", "id2"));

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "id1", "id2"));
    }

    @Test
    void build__with_empty_ids_list__should_return_spec_without_ids() {
        var subject = new HtCliRmSpec(List.of());

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm"));
    }

    @Test
    void build__with_two_vararg_id__should_return_spec_with_ids() {
        var subject = new HtCliRmSpec("id1", "id2");

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "id1", "id2"));
    }

    @Test
    void build__with_one_vararg_id__should_return_spec_with_one_id() {
        var subject = new HtCliRmSpec("id1");

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "id1"));
    }

    @Test
    void build__with_force_true__should_return_spec_with_force() {
        var subject = new HtCliRmSpec().withForce(true);

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "--force"));
    }

    @Test
    void build__with_volumes_true__should_return_spec_with_volumes() {
        var subject = new HtCliRmSpec().withVolumes(true);

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "--volumes"));
    }

    @Test
    void build__with_two_ids_and_force_and_volumes__should_return_spec_with_ids_force_and_volumes() {
        var subject = new HtCliRmSpec(List.of("id1", "id2")).withForce(true).withVolumes(true);

        var actual = subject.build();

        assertThat(actual).isEqualTo(List.of("docker", "rm", "--force", "--volumes", "id1", "id2"));
    }
}
