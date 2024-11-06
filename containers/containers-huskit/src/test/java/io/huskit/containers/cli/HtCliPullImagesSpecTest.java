package io.huskit.containers.cli;

import io.huskit.containers.api.image.HtImgName;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtCliPullImagesSpecTest implements UnitTest {

    HtImgName imageName = HtImgName.of("some:id");

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void toCommand__blank_string__should_throw_exception(String ref) {
        assertThatThrownBy(() -> new HtCliPullImagesSpec(HtImgName.of(ref)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Image reference cannot be blank");
    }

    @Test
    void toCommand__with_image_id__should_return_spec_with_image_id() {
        var subject = new HtCliPullImagesSpec(imageName);

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", imageName.reference());
    }

    @Test
    void toCommand__with_all_tags_true__should_return_spec_with_all_tags() {
        var subject = new HtCliPullImagesSpec(imageName).withAllTags();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--all-tags", imageName.reference());
    }

    @Test
    void toCommand__with_disable_content_trust_true__should_return_spec_with_disable_content_trust() {
        var subject = new HtCliPullImagesSpec(imageName).withDisableContentTrust();

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--disable-content-trust", imageName.reference());
    }

    @Test
    void toCommand__with_platform__should_return_spec_with_platform() {
        var subject = new HtCliPullImagesSpec(imageName).withPlatformString("platform");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--platform", "platform", imageName.reference());
    }

    @Test
    void toCommand__with_all_tags_and_disable_content_trust_and_platform__should_return_spec_with_all_tags_and_disable_content_trust_and_platform() {
        var subject = new HtCliPullImagesSpec(imageName)
            .withAllTags()
            .withDisableContentTrust()
            .withPlatformString("platform");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly(
            "docker", "pull",
            "--all-tags",
            "--disable-content-trust",
            "--platform",
            "platform",
            imageName.reference()
        );
    }

    @Test
    void toCommand__with_all_tags_and_platform__should_return_spec_with_all_tags_and_platform() {
        var subject = new HtCliPullImagesSpec(imageName)
            .withAllTags()
            .withPlatformString("platform");

        var actual = subject.toCommand();

        assertThat(actual).containsExactly("docker", "pull", "--all-tags", "--platform", "platform", imageName.reference());
    }
}
