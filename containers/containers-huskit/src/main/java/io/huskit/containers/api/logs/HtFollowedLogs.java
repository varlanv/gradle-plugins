package io.huskit.containers.api.logs;

import java.util.stream.Stream;

public interface HtFollowedLogs {

    Stream<String> stream();

    HtFollowedLogs lookFor(LookFor lookFor);
}
