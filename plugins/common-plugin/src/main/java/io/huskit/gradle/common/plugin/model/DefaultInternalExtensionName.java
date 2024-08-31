package io.huskit.gradle.common.plugin.model;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class DefaultInternalExtensionName implements CharSequence {

    private final CharSequence original;

    public static String value(CharSequence original) {
        return String.format("__huskit_%s__", original);
    }

    @Override
    public int length() {
        return value(original).length();
    }

    @Override
    public char charAt(int index) {
        return value(original).charAt(index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return value(original).subSequence(start, end);
    }

    @Nonnull
    @Override
    public String toString() {
        return super.toString();
    }
}
