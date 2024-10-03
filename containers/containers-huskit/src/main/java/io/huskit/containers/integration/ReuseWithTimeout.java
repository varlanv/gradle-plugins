package io.huskit.containers.integration;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class ReuseWithTimeout {

    Boolean enabled;
    Duration cleanupAfter;
}
