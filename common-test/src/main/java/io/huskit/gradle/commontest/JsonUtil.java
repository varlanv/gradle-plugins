package io.huskit.gradle.commontest;

import groovy.json.JsonSlurper;
import lombok.experimental.UtilityClass;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@UtilityClass
public final class JsonUtil {

    @SuppressWarnings({"unchecked", "rawtypes"})
    <T> T getJsonField(String json, String field, Class<T> type) {
        var parsed = new JsonSlurper().parseText(json);
        assertThat(parsed).isInstanceOf(Map.class);
        var parsedMap = (Map) parsed;
        var fieldValue = parsedMap.get(field);
        assertThat(fieldValue).satisfiesAnyOf(
            it -> assertThat(it).isNull(),
            it -> assertThat(it).isInstanceOf(type)
        );
        return (T) fieldValue;
    }
}
