package io.huskit.containers.model.id;

import io.huskit.gradle.commontest.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMapContainerKeyTest implements BaseTest {

    @Test
    @DisplayName("`json` if no properties, should return empty json")
    void json_if_no_properties_should_return_empty_json() {
        var subject = new JsonMapContainerKey();

        var actual = subject.json();

        assertEquals("{}", actual);
    }

    @Test
    @DisplayName("`json` if has properties, should return json")
    void json_if_has_properties_should_return_json() {
        var subject = new JsonMapContainerKey(
                Map.of("key1", "value",
                        "key2", 1)
        );

        var actual = subject.json();

        assertThat(actual).isEqualTo("{\"key1\": \"value\", \"key2\": \"1\"}");
    }

    @Test
    @DisplayName("`json` if try to add map empty map, should return empty json")
    void json_if_try_to_add_map_empty_map_should_return_empty_json() {
        var subject = new JsonMapContainerKey().with(Map.of());

        var actual = subject.json();

        assertThat(actual).isEqualTo("{}");
    }

    @Test
    @DisplayName("`json` when adding map with unsorted keys, should return sorted json")
    void json_when_adding_map_with_unsorted_keys_should_return_sorted_json() {
        var subject = new JsonMapContainerKey().with(
                Map.of(
                        "key2", "value2",
                        "key1", "value1",
                        "key3", "value3"
                )
        );

        var actual = subject.json();

        assertThat(actual).isEqualTo("{\"key1\": \"value1\", \"key2\": \"value2\", \"key3\": \"value3\"}");
    }

    @Test
    @DisplayName("`with` if add non null entry, then should return new instance with entry")
    void with_if_add_non_null_entry_then_should_return_new_instance_with_entry() {
        var subject = new JsonMapContainerKey();

        var newSubject = subject.with("key42", 42);

        assertThat(newSubject.json()).isEqualTo("{\"key42\": \"42\"}");
    }
}
