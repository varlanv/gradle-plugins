package io.huskit.containers.api.image;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface HtListImages {

    Stream<HtImageView> stream();

    default List<HtImageView> collect() {
        return stream().collect(Collectors.toList());
    }

    default HtImageView requireSingle() {
        var images = collect();
        if (images.isEmpty()) {
            throw new IllegalStateException("No images found");
        } else if (images.size() > 1) {
            throw new IllegalStateException(String.format("Found [%s] images while expecting only one. Image ids: [%s]",
                images.size(), images.stream().map(HtImageView::shortId).collect(Collectors.joining(", "))));
        } else {
            return images.get(0);
        }
    }
}
