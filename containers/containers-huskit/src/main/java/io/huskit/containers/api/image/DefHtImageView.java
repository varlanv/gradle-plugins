package io.huskit.containers.api.image;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Supplier;

@RequiredArgsConstructor
@ToString(of = "shortId")
public class DefHtImageView implements HtImageView {

    @Getter
    String shortId;
    Supplier<HtImageRichView> richViewSupplier;

    @Override
    public HtImageRichView inspect() {
        return richViewSupplier.get();
    }
}
