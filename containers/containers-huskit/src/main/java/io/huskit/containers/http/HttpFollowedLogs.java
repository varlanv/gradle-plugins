package io.huskit.containers.http;

import io.huskit.containers.api.container.logs.HtFollowedLogs;
import io.huskit.containers.api.container.logs.LookFor;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

@RequiredArgsConstructor
class HttpFollowedLogs implements HtFollowedLogs {

    HtHttpDockerSpec dockerSpec;
    String containerId;

    @Override
    public Stream<String> stream() {
        return Stream.empty();
    }

    @Override
    public HtFollowedLogs lookFor(LookFor lookFor) {
        return null;
    }
}
