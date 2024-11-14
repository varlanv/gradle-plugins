package io.huskit.containers.http;

import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PushJsonObjectTest implements UnitTest {

    @Test
    void push__when_body_is_present_and_empty_object__should_return_empty() {
        var subject = new PushJsonObject();

        var maybeActual = subject.push(ByteBuffer.wrap("{}".getBytes()));

        assertThat(maybeActual)
            .hasValueSatisfying(
                actual -> assertThat(actual).isEmpty()
            );
    }

    @Test
    void value__when_not_pushed__is_empty() {
        var subject = new PushJsonObject();

        assertThat(subject.value()).isEmpty();
    }

    @Test
    void push__when_body_is_present_and_object_with_one_element__should_return_object_with_one_element() {
        var subject = new PushJsonObject();

        var maybeActual = subject.push(ByteBuffer.wrap("{\"key\":\"value\"}".getBytes()));

        assertThat(maybeActual)
            .hasValueSatisfying(
                actual -> assertThat(actual)
                    .hasSize(1)
                    .containsEntry("key", "value")
            );
    }

    @Test
    void push__when_body_is_invalid_json__should_throw_exception() {
        var subject = new PushJsonObject();

        assertThatThrownBy(
            () -> subject.push(ByteBuffer.wrap("invalid json".getBytes()))
        ).isInstanceOf(RuntimeException.class);
    }

    @Test
    void value__when_pushed__should_return_pushed_value() {
        var subject = new PushJsonObject();

        subject.push(ByteBuffer.wrap("{\"key\":\"value\"}".getBytes()));

        assertThat(subject.value())
            .hasValueSatisfying(
                actual -> assertThat(actual)
                    .hasSize(1)
                    .containsEntry("key", "value")
            );
    }

    @Test
    void push__when_result_was_already_set__should_throw_exception() {
        var subject = new PushJsonObject();

        subject.push(ByteBuffer.wrap("{}".getBytes()));

        assertThatThrownBy(
            () -> subject.push(ByteBuffer.wrap("{}".getBytes()))
        ).isInstanceOf(IllegalStateException.class);
    }
}
