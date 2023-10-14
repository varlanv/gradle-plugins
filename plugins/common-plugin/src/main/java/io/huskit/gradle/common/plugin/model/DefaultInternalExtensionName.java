package io.huskit.gradle.common.plugin.model;

public class DefaultInternalExtensionName {

    public static String value(String val) {
        return String.format("__huskit_%s__", val);
    }
}
