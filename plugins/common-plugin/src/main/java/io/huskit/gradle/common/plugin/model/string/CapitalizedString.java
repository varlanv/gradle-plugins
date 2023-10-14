package io.huskit.gradle.common.plugin.model.string;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class CapitalizedString implements CharSequence {

    private final String original;

    public static String capitalize(String original) {
        char upperCase = Character.toUpperCase(original.charAt(0));
        return upperCase + original.substring(1);
    }

    @Override
    public int length() {
        return original.length();
    }

    @Override
    public char charAt(int i) {
        return i == 0 ? Character.toUpperCase(original.charAt(i)) : original.charAt(i);
    }

    @NotNull
    @Override
    public CharSequence subSequence(int i, int i1) {
        return i == 0 ? this.toString().subSequence(i, i1) : original.subSequence(i, i1);
    }

    @Override
    public String toString() {
        return capitalize(original);
    }

    @Override
    public int hashCode() {
        return capitalize(original).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return capitalize(original).equals(obj);
    }
}
