package io.huskit.containers.model.image;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class DefaultContainerImage implements ContainerImage {

    CharSequence charSequence;

    @Override
    public String id() {
        return charSequence.toString();
    }
}
