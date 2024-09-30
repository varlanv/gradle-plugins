package io.huskit.containers.api.image;

public interface HtListImageFilter {

    HtListImages byBefore(String image);

    HtListImages bySince(String image);

    HtListImages byReference(String reference);

    HtListImages byUntil(String image);

    HtListImages byDangling(Boolean dangling);

    HtListImages byLabel(String key);

    HtListImages byLabel(String key, String value);
}
