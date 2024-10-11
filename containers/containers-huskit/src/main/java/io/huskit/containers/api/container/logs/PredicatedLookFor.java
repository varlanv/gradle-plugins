package io.huskit.containers.api.container.logs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;

import java.time.Duration;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public class PredicatedLookFor implements LookFor {

    Predicate<String> predicate;
    @With
    Duration timeout;
}
