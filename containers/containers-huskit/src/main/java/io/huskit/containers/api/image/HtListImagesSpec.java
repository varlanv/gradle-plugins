package io.huskit.containers.api.image;

public interface HtListImagesSpec {

    HtListImagesSpec withAll();

    HtListImagesSpec withFilterByBefore(String image);

    HtListImagesSpec withFilterBySince(String image);

    HtListImagesSpec withFilterByReference(String reference);

    HtListImagesSpec withFilterByUntil(String image);

    HtListImagesSpec withFilterByDangling(Boolean dangling);

    HtListImagesSpec withFilterByLabel(String key);

    HtListImagesSpec withFilterByLabel(String key, String value);
}
