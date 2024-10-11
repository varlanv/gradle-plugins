package io.huskit.containers.api.container.logs;

import java.util.stream.Stream;

public interface HtLogs {

    Stream<String> stream();

    HtFollowedLogs follow();
}
