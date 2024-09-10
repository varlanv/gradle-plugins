package io.huskit.common;

import lombok.NoArgsConstructor;

@SuppressWarnings({"unchecked"})
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Nothing {

    private static final Nothing INSTANCE = new Nothing();

    public static <T> T instance() {
        return (T) INSTANCE;
    }
}
