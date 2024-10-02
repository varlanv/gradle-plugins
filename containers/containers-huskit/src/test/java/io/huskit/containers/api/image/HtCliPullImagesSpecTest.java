package io.huskit.containers.api.image;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtCliPullImagesSpecTest implements UnitTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void toCommand__blank_string__should_throw_exception(String id) {
        assertThatThrownBy(() -> new HtCliPullImagesSpec(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image ID cannot be blank");
    }

    @Test
    void toCommand__with_image_id__should_return_spec_with_image_id() {
        var subject = new HtCliPullImagesSpec("someid");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "someid");
    }

    @Test
    void toCommand__with_all_tags_true__should_return_spec_with_all_tags() {
        var subject = new HtCliPullImagesSpec("someid").withAllTags();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--all-tags", "someid");
    }

    @Test
    void toCommand__with_disable_content_trust_true__should_return_spec_with_disable_content_trust() {
        var subject = new HtCliPullImagesSpec("someid").withDisableContentTrust();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--disable-content-trust", "someid");
    }

    @Test
    void toCommand__with_platform__should_return_spec_with_platform() {
        var subject = new HtCliPullImagesSpec("someid").withPlatformString("platform");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--platform", "platform", "someid");
    }

    @Test
    void toCommand__with_all_tags_and_disable_content_trust_and_platform__should_return_spec_with_all_tags_and_disable_content_trust_and_platform() {
        var subject = new HtCliPullImagesSpec("someid")
                .withAllTags()
                .withDisableContentTrust()
                .withPlatformString("platform");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--all-tags", "--disable-content-trust", "--platform", "platform", "someid");
    }

    @Test
    void toCommand__with_all_tags_and_platform__should_return_spec_with_all_tags_and_platform() {
        var subject = new HtCliPullImagesSpec("someid")
                .withAllTags()
                .withPlatformString("platform");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--all-tags", "--platform", "platform", "someid");
    }
}
