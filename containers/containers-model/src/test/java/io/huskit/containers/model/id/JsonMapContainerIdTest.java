package io.huskit.containers.model.id;

import io.huskit.gradle.commontest.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonMapContainerIdTest implements BaseTest {

    @Test
    @DisplayName("`json` if no properties, should return empty json")
    void test_0() {
        var subject = new JsonMapContainerId();

        var actual = subject.json();

        assertEquals("{}", actual);
    }

    @Test
    @DisplayName("`json` if has properties, should return json")
    void test_1() {
        var subject = new JsonMapContainerId(
                Map.of("key1", "value",
                        "key2", 1)
        );

        var actual = subject.json();

        assertThat(actual).isEqualTo("{\"key1\": \"value\", \"key2\": \"1\"}");
    }

    @Test
    @DisplayName("`json` if try to add null value, should not throw exception")
    void test_3() {
        var subject = new JsonMapContainerId().with("key", null);

        var actual = subject.json();

        assertThat(actual).isEqualTo("{\"key\": \"null\"}");
    }

    @Test
    @DisplayName("`json` if try to add map with null value, should not throw exception")
    void test_4() {
        var props = new HashMap<String, Object>();
        props.put("key", null);
        var subject = new JsonMapContainerId().with(props);

        var actual = subject.json();

        assertThat(actual).isEqualTo("{\"key\": \"null\"}");
    }

    @Test
    @DisplayName("`json` if try to add map empty map, should return empty json")
    void test_5() {
        var subject = new JsonMapContainerId().with(Map.of());

        var actual = subject.json();

        assertThat(actual).isEqualTo("{}");
    }

    @Test
    @DisplayName("`json` when adding map with unsorted keys, should return sorted json")
    void test_6() {
        var subject = new JsonMapContainerId().with(
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
    @DisplayName("`with` if try to add null key, should throw exception")
    void test_7() {
        var subject = new JsonMapContainerId();
        assertThatThrownBy(() -> subject.with(null, "value"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("`with` if add non null entry, then should return new instance with entry")
    void test_8() {
        var subject = new JsonMapContainerId();

        var newSubject = subject.with("key42", 42);

        assertThat(newSubject.json()).isEqualTo("{\"key42\": \"42\"}");
    }

    @Test
    @DisplayName("`with` if try to add map with null key, should throw exception")
    void test_9() {
        var subject = new JsonMapContainerId();
        var props = new HashMap<String, Object>();
        props.put(null, "value");
        assertThatThrownBy(() -> subject.with(props))
                .isInstanceOf(NullPointerException.class);
    }
}
