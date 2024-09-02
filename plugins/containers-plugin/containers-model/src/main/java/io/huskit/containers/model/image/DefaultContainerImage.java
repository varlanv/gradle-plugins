package io.huskit.containers.model.image;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultContainerImage implements ContainerImage {

    CharSequence charSequence;

    @Override
    public String value() {
        return charSequence.toString();
    }
}
