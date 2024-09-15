package io.huskit.containers.integration.mongo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class Waiter {

    String text;
    Duration duration;
}
