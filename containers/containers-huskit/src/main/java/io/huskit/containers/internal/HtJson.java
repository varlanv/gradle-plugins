package io.huskit.containers.internal;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;

import java.util.Map;

@UtilityClass
public class HtJson {

    public static Map<String, Object> toMap(String json) {
        return new JSONObject(json).toMap();
    }
}
