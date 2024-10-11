package io.huskit.containers.api.image;

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
