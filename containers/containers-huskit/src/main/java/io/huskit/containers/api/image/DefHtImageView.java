package io.huskit.containers.api.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class DefHtImageView implements HtImageView {

    @Getter
    String shortId;
    Supplier<HtImageRichView> richViewSupplier;

    @Override
    public HtImageRichView inspect() {
        return richViewSupplier.get();
    }
}
