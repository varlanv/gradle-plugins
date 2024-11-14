package io.huskit.containers.http;

import io.huskit.common.Sneaky;
import io.huskit.gradle.commontest.UnitTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class PushJsonArrayTest implements UnitTest {

    byte[] body = Sneaky.quiet(() -> IOUtils.resourceToByteArray("/docker_http_responses/containers/chunked_json_body.txt"));

    @Test
    void apply__when_body_is_present__should_return_json() {
        var subject = new PushChunked<>(new PushJsonArray());

        var maybeActual = subject.apply(ByteBuffer.wrap(body));

        assertThat(maybeActual)
            .hasValueSatisfying(
                actual -> assertThat(actual).hasSize(4)
            );
    }
}
