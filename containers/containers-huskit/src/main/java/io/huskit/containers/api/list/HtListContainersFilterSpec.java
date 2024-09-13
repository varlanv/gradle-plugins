package io.huskit.containers.api.list;

public interface HtListContainersFilterSpec {

    HtListContainersFilter id(CharSequence id);

    HtListContainersFilter name(CharSequence name);

    HtListContainersFilter label(CharSequence label, CharSequence value);
}
