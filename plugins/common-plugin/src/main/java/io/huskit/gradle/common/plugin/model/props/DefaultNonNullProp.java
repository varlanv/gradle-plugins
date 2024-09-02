package io.huskit.gradle.common.plugin.model.props;

import io.huskit.gradle.common.plugin.model.props.exception.NonNullPropertyException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class DefaultNonNullProp implements NonNullProp {

    NullableProp nullableProp;

    @Override
    public String name() {
        return nullableProp.name();
    }

    @Override
    public @Nullable Object value() {
        Object value = nullableProp.value();
        checkNull(value == null);
        return value;
    }

    @Override
    public @Nullable String stringValue() {
        String value = nullableProp.stringValue();
        checkNull(value == null);
        return value;
    }

    private void checkNull(boolean value) {
        if (value) {
            throw new NonNullPropertyException(name());
        }
    }
}
