package io.huskit.containers.internal;

import lombok.experimental.UtilityClass;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.stream.Stream;

@UtilityClass
public class HtJson {

    public static Map<String, Object> toMap(String json) {
        return new JSONObject(json).toMap();
    }

    @SuppressWarnings("unchecked")
    public static Stream<Map<String, Object>> toMapStream(String json) {
        var list = new JSONArray(json).toList();
        return list.stream().map(o -> (Map<String, Object>) o);
    }

    public static String toJson(Map<String, Object> map) {
        return new JSONObject(map).toString();
    }
}
