package io.huskit.gradle.common.plugin.model.props;

import io.huskit.gradle.common.plugin.model.DefaultInternalExtensionName;

public interface Props {

    String EXTENSION_NAME = DefaultInternalExtensionName.value("props");

    boolean hasProp(String name);

    NonNullProp nonnull(String name);

    NullableProp nullable(String name);

    NullableProp env(String name);
}
