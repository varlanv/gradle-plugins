package io.huskit.gradle.common.plugin.model;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class DefaultInternalExtensionName implements CharSequence {

    private final CharSequence original;

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    @Nonnull
    @Override
    public String toString() {
        return String.format("__huskit_%s__", original);
    }
}
