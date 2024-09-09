package io.huskit.common;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Tuple<V1, V2> {

    V1 left;
    V2 right;

    public List<?> toList() {
        return List.of(left, right);
    }
}
