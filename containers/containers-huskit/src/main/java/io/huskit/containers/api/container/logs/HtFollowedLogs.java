package io.huskit.containers.api.container.logs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HtFollowedLogs {

    Stream<String> stream();

    HtFollowedLogs lookFor(LookFor lookFor);

    default List<String> await() {
        return stream().collect(Collectors.toList());
    }
}
