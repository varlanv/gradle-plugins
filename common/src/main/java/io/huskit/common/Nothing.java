package io.huskit.common;

@SuppressWarnings({"unchecked"})
public class Nothing {

    private static final Nothing INSTANCE = new Nothing();

    public static <T> T instance() {
        return (T) INSTANCE;
    }
}
