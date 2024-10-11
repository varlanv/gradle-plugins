package io.huskit.containers.api.container.list.arg;

public interface HtListContainersArgsSpec {

    HtListContainersArgsSpec withAll();

    HtListContainersArgsSpec withIdFilter(CharSequence id);

    HtListContainersArgsSpec withNameFilter(CharSequence name);

    HtListContainersArgsSpec withLabelFilter(CharSequence label, CharSequence value);
}
