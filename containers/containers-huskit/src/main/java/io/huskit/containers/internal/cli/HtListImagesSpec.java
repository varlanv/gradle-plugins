package io.huskit.containers.internal.cli;

public interface HtListImagesSpec {

    HtCliListImagesSpec withAll();

    HtCliListImagesSpec withFilterByBefore(String image);

    HtCliListImagesSpec withFilterBySince(String image);

    HtCliListImagesSpec withFilterByReference(String reference);

    HtCliListImagesSpec withFilterByUntil(String image);

    HtCliListImagesSpec withFilterByDangling(Boolean dangling);

    HtCliListImagesSpec withFilterByLabel(String key);

    HtCliListImagesSpec withFilterByLabel(String key, String value);
}
