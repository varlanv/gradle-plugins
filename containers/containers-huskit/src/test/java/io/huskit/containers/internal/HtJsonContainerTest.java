package io.huskit.containers.internal;

import io.huskit.containers.api.container.HtJsonContainer;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtJsonContainerTest implements UnitTest {

    @Test
    void id__should__get_id_from_map() {
        // given
        var container = new HtJsonContainer(Map.of("Id", "someId"));

        // then
        assertThat(container.id()).isEqualTo("someId");
    }

    @Test
    void id__if__id_is_null__should__throw_exception() {
        // given
        var container = new HtJsonContainer(Map.of());

        // then
        assertThatThrownBy(container::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("Id");
    }

    @Test
    void name__should__get_name_from_map() {
        // given
        var container = new HtJsonContainer(Map.of("Name", "someName"));

        // then
        assertThat(container.name()).isEqualTo("someName");
    }

    @Test
    void name__if__name_is_null__should__throw_exception() {
        // given
        var container = new HtJsonContainer(Map.of());

        // then
        assertThatThrownBy(container::name)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("Name");
    }

    @Test
    void labels__should__get_labels_from_map() {
        // given
        var container = new HtJsonContainer(Map.of(
                "Config", Map.of("Labels", Map.of("key", "value"))
        ));

        // then
        assertThat(container.config().labels()).containsOnly(Map.entry("key", "value"));
    }

    @Test
    void labels__if__labels_is_null__should__throw_exception() {
        // given
        var container = new HtJsonContainer(Map.of(
                "Config", Map.of()
        ));

        // then
        assertThatThrownBy(container.config()::labels)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("Labels");
    }

    @Test
    void labels__if__config_is_null__should__throw_exception() {
        // given
        var container = new HtJsonContainer(Map.of());

        // then
        assertThatThrownBy(() -> container.config().labels())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContainingAll("Config");
    }
}
