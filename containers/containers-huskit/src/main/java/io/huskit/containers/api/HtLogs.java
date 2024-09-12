package io.huskit.containers.api;

import io.huskit.containers.api.logs.LookFor;

import java.util.stream.Stream;

public interface HtLogs {

    Stream<String> stream();

    HtLogs lookFor(LookFor lookFor);
}
