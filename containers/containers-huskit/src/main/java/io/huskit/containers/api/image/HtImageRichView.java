package io.huskit.containers.api.image;

import java.util.stream.Stream;

public interface HtImageRichView {

    String id();

    Stream<ImageTag> tags();
}
