package io.huskit.containers.api.image;

import java.util.ArrayList;
import java.util.List;

public interface HtImgName {

    String reference();

    String repository();

    String tag();

    static List<HtImgName> of(Iterable<? extends CharSequence> references) {
        var list = new ArrayList<HtImgName>();
        for (var ref : references) {
            list.add(of(ref));
        }
        return list;
    }

    static HtImgName of(CharSequence reference) {
        var ref = reference.toString();
        if (ref.isBlank()) {
            throw new IllegalArgumentException("Image reference cannot be blank");
        }
        var split = ref.split(":");
        if (split.length != 2) {
            throw new IllegalArgumentException(
                    String.format("Could not parse image reference. Expecting reference in format 'repository:tag', got '%s'", ref));
        }
        return new HtDefImgName(ref, split[0], split[1]);
    }

    static HtImgName of(CharSequence repository, CharSequence tag) {
        var repo = repository.toString();
        var t = tag.toString();
        if (repo.isBlank()) {
            throw new IllegalArgumentException("Image repository cannot be blank");
        }
        if (t.isBlank()) {
            throw new IllegalArgumentException("Image tag cannot be blank");
        }
        return new HtDefImgName(repo + ":" + t, repo, t);
    }

    static List<HtImgName> ofPrefix(CharSequence prefix, Iterable<? extends CharSequence> references) {
        var list = new ArrayList<HtImgName>();
        for (var ref : references) {
            list.add(ofPrefix(prefix, ref));
        }
        return list;
    }

    static HtImgName ofPrefix(CharSequence prefix, CharSequence reference) {
        if (prefix.length() == 0) {
            return of(reference);
        }
        var p = prefix.toString();
        if (p.isBlank()) {
            throw new IllegalArgumentException("Image prefix cannot be blank");
        }
        return of(p + reference);
    }
}
