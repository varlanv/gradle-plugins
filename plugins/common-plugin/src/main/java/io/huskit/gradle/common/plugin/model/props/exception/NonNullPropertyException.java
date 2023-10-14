package io.huskit.gradle.common.plugin.model.props.exception;

public class NonNullPropertyException extends RuntimeException {

    public NonNullPropertyException(String propertyName) {
        super("Property '" + propertyName + "' is null, but non-null value is expected");
    }
}
