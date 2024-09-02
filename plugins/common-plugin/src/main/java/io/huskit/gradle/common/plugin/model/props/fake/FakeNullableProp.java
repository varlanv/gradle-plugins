package io.huskit.gradle.common.plugin.model.props.fake;

import io.huskit.gradle.common.plugin.model.props.NullableProp;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter
@RequiredArgsConstructor
public class FakeNullableProp implements NullableProp {

    String name;
    Object value;

    @Override
    public @Nullable String stringValue() {
        return Objects.toString(value(), null);
    }
}
