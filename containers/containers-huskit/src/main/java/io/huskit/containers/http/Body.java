package io.huskit.containers.http;

import java.util.List;
import java.util.stream.Stream;

public interface Body {

    List<String> list();

    Stream<String> stream();

    String singleLine();
}
