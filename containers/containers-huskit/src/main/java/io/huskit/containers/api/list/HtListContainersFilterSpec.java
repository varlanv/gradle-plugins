package io.huskit.containers.api.list;

public interface HtListContainersFilterSpec {

    HtListContainersFilterSpec id(CharSequence id);

    HtListContainersFilterSpec name(CharSequence name);

    HtListContainersFilterSpec label(CharSequence label, CharSequence value);
}
