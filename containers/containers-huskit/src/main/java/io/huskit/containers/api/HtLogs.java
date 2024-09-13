package io.huskit.containers.api;

import java.util.stream.Stream;

public interface HtLogs {

    Stream<String> stream();

    HtFollowedLogs follow();
}
