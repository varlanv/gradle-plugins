package io.huskit.common.port;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Optional;

@RequiredArgsConstructor
@ToString(of = {"hostValue", "containerValue", "isFixed"})
@EqualsAndHashCode(of = {"hostValue", "containerValue", "isFixed"})
public class ResolvedPort implements ContainerPort {

    @Getter
    Integer hostValue;
    Integer containerValue;
    @Getter
    Boolean isFixed;

    @Override
    public Optional<Integer> containerValue() {
        return Optional.of(containerValue);
    }
}
