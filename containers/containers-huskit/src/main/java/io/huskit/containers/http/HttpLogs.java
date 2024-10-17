package io.huskit.containers.http;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.HtLogs;

import java.io.StringReader;
import java.util.Arrays;
import java.util.stream.Stream;

public class HttpLogs implements HtLogs {

    HtHttpDockerSpec dockerSpec;
    String containerId;

    public HttpLogs(HtHttpDockerSpec dockerSpec, CharSequence containerId) {
        this.dockerSpec = dockerSpec;
        this.containerId = containerId.toString();
    }

    @Override
    public Stream<String> stream() {
        var response = dockerSpec.socket().send(
                new HttpLogsSpec(containerId).toRequest(),
                r -> Arrays.asList(r.reader().toString().split("\n"))
        );
        if (response.head().status() != 200) {
            throw new RuntimeException(
                    String.format(
                            "Failed to get logs for container '%s', received status %d - %s",
                            containerId, response.head().status(), response.body().list()
                    )
            );
        }
        return response.body().stream();
    }

    @Override
    public HtFollowedLogs follow() {
        return new HttpFollowedLogs(dockerSpec, containerId);
    }
}
