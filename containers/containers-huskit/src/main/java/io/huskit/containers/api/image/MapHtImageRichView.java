package io.huskit.containers.api.image;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class MapHtImageRichView implements HtImageRichView {

    Map<String, Object> map;

    @Override
    public String id() {
        var id = (String) map.get("Id");
        return id.substring(7);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<ImageTag> tags() {
        var repoTags = (List<String>) map.get("RepoTags");
        return repoTags.stream()
            .map(tag -> {
                var parts = tag.split(":");
                return new DefImageTag(parts[0], parts[1]);
            });
    }
}
