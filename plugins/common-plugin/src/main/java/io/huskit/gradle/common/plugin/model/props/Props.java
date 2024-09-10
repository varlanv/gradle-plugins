package io.huskit.gradle.common.plugin.model.props;

import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;

public interface Props {

    static String name() {
        return new DefaultInternalExtensionName("props").toString();
    }

    boolean hasProp(CharSequence name);

    NonNullProp nonnull(CharSequence name);

    NullableProp nullable(CharSequence name);

    NullableProp env(CharSequence name);
}
