package io.huskit.common;

import lombok.*;

import java.util.List;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
public class Tuple<V1, V2> {

    @NonNull
    V1 left;
    @NonNull
    V2 right;

    public List<?> toList() {
        return List.of(left, right);
    }
}
