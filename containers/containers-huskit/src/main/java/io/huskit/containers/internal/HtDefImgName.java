package io.huskit.containers.internal;

import io.huskit.containers.api.HtImgName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString(of = "reference")
@EqualsAndHashCode(of = "reference")
public class HtDefImgName implements HtImgName {

    String reference;
    String repository;
    String tag;
}
