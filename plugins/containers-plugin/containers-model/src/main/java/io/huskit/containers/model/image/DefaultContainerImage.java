package io.huskit.containers.model.image;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultContainerImage implements ContainerImage {

    private final CharSequence charSequence;

    @Override
    public String value() {
        return charSequence.toString();
    }
}
