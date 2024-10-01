package io.huskit.containers.api.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DefImageTag implements ImageTag {

    String repository;
    String tag;
}
