package io.huskit.containers.api.ps;

public interface HtPsFilterBuilder {

    HtPsFilter id(CharSequence id);

    HtPsFilter name(CharSequence name);

    HtPsFilter label(CharSequence label);
}
