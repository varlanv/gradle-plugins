package io.huskit.containers.api;

import io.huskit.containers.api.logs.LookFor;

import java.util.stream.Stream;

public interface HtFollowedLogs {

    Stream<String> stream();

    HtFollowedLogs lookFor(LookFor lookFor);
}
