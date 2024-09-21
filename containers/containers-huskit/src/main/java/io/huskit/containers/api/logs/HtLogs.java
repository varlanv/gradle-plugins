package io.huskit.containers.api.logs;

import java.util.stream.Stream;

public interface HtLogs {

    Stream<String> stream();

    HtFollowedLogs follow();
}
