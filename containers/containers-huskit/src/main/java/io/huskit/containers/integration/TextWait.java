package io.huskit.containers.integration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

@Getter
@RequiredArgsConstructor
public class TextWait {

    String text;
    Duration duration;
}
