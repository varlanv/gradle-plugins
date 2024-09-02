package io.huskit.log.fake;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class FakeLoggedMessage {

    String message;
    List<String> args;
    String level;

    public FakeLoggedMessage(String message, String level) {
        this(message, List.of(), level);
    }
}
