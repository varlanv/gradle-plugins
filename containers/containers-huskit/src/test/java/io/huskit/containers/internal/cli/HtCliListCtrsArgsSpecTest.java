package io.huskit.containers.internal.cli;

import io.huskit.common.HtStrings;
import io.huskit.containers.api.cli.HtArg;
import io.huskit.gradle.commontest.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class HtCliListCtrsArgsSpecTest implements UnitTest {

    @Test
    void build__default__should_include_only_default_format() {
        var subject = new HtCliListCtrsArgsSpec();

        var actual = subject.build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isOne();
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(1);
        verifyFormat(args.get(0));
    }

    @Test
    void build__withAll__should_include_all_ar() {
        var subject = new HtCliListCtrsArgsSpec();

        var actual = subject.withAll().build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(2);
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(2);
        verifyAll(args.get(0));
        verifyFormat(args.get(1));
    }

    @Test
    void build__withIdFilter__should_include_id_filter() {
        var subject = new HtCliListCtrsArgsSpec();
        var id = "some-id";

        var actual = subject.withIdFilter(id).build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(2);
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(2);
        verifyFilter(args.get(0), id);
        verifyFormat(args.get(1));
    }

    @Test
    void build__withLabelFilter__one_label__should_include_label_filter() {
        var subject = new HtCliListCtrsArgsSpec();
        var label = "some-label";
        var value = "some-value";

        var actual = subject.withLabelFilter(label, value).build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(2);
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(2);
        verifyLabel(args.get(0), label, value);
        verifyFormat(args.get(1));
    }

    @Test
    void build__withLabelFilter__two_labels__should_include_both_label_filters() {
        var subject = new HtCliListCtrsArgsSpec();
        var label1 = "some-label-1";
        var value1 = "some-value-1";
        var label2 = "some-label-2";
        var value2 = "some-value-2";

        var actual = subject.withLabelFilter(label1, value1).withLabelFilter(label2, value2)
                .build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(3);
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(3);
        verifyLabel(args.get(0), label1, value1);
        verifyLabel(args.get(1), label2, value2);
        verifyFormat(args.get(2));
    }

    @Test
    void build__withNameFilter__should_include_name_filter() {
        var subject = new HtCliListCtrsArgsSpec();
        var name = "some-name";

        var actual = subject.withNameFilter(name).build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(2);
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(2);
        verifyName(args.get(0), name);
        verifyFormat(args.get(1));
    }

    @Test
    void build__withAll_withNameFilter_withLabelFilter__should_include_all() {
        var subject = new HtCliListCtrsArgsSpec();
        var name = "some-name";
        var id = "some-id";
        var label1 = "some-label";
        var value1 = "some-value";
        var label2 = "some-label-2";
        var value2 = "some-value-2";

        var actual = subject.withAll().withNameFilter(name).withIdFilter(id)
                .withLabelFilter(label1, value1).withLabelFilter(label2, value2)
                .build();

        assertThat(actual).isNotNull();
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(6);
        var args = actual.stream().collect(Collectors.toList());
        assertThat(args).hasSize(6);
        verifyAll(args.get(0));
        verifyName(args.get(1), name);
        verifyFilter(args.get(2), id);
        verifyLabel(args.get(3), label1, value1);
        verifyLabel(args.get(4), label2, value2);
        verifyFormat(args.get(5));
    }

    private void verifyName(HtArg htArg, String name) {
        assertThat(htArg.name()).isEqualTo("--filter");
        assertThat(htArg.singleValue()).isEqualTo(HtStrings.doubleQuote("name=%s"), name);
    }

    private void verifyLabel(HtArg htArg, String label, String value) {
        assertThat(htArg.name()).isEqualTo("--filter");
        assertThat(htArg.singleValue()).isEqualTo(HtStrings.doubleQuote("label=%s=%s"), label, value);
    }

    private void verifyFilter(HtArg htArg, String id) {
        assertThat(htArg.name()).isEqualTo("--filter");
        assertThat(htArg.singleValue()).isEqualTo(HtStrings.doubleQuote("id=%s"), id);
    }

    private void verifyAll(HtArg arg) {
        assertThat(arg.name()).isEqualTo("-a");
        assertThat(arg.values()).isEmpty();
    }

    private void verifyFormat(HtArg arg) {
        assertThat(arg.name()).isEqualTo("--format");
        assertThat(arg.singleValue()).isEqualTo(HtStrings.doubleQuote("{{json .}}"));
    }
}
