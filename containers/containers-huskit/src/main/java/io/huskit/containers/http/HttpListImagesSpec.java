package io.huskit.containers.http;

import io.huskit.common.collection.HtCollections;
import io.huskit.containers.api.image.HtListImagesSpec;
import io.huskit.containers.internal.HtJson;
import lombok.experimental.NonFinal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final class HttpListImagesSpec implements HtListImagesSpec, HtUrl {

    Map<String, List<String>> filters = new HashMap<>();
    @NonFinal
    boolean all;

    @Override
    public HttpListImagesSpec withAll() {
        this.all = true;
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterByBefore(String image) {
        HtCollections.putOrAdd(filters, "before", image);
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterBySince(String image) {
        HtCollections.putOrAdd(filters, "since", image);
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterByReference(String reference) {
        HtCollections.putOrAdd(filters, "reference", reference);
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterByUntil(String image) {
        HtCollections.putOrAdd(filters, "until", image);
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterByDangling(Boolean dangling) {
        HtCollections.putOrAdd(filters, "dangling", dangling.toString());
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterByLabel(String key) {
        HtCollections.putOrAdd(filters, "label", key);
        return this;
    }

    @Override
    public HttpListImagesSpec withFilterByLabel(String key, String value) {
        HtCollections.putOrAdd(filters, "label", key + "=" + value);
        return this;
    }

    @Override
    public String url() {
        return "/images/json" + toParameters();
    }

    public String toParameters() {
        var parameters = new ArrayList<String>();
        if (all) {
            parameters.add("all=true");
        }
        if (!filters.isEmpty()) {
            var jsonObject = HtJson.toJson(
                filters.entrySet().stream()
                    .collect(
                        Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue
                        )
                    )
            );
            parameters.add("filters=" + jsonObject);
        }
        if (parameters.isEmpty()) {
            return "";
        } else {
            return "?" + String.join("&", parameters);
        }
    }
}
