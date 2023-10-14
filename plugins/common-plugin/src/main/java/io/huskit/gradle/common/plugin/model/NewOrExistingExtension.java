package io.huskit.gradle.common.plugin.model;

import lombok.RequiredArgsConstructor;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionContainer;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class NewOrExistingExtension {

    private final Logger log = Logging.getLogger(NewOrExistingExtension.class);
    private final ExtensionContainer extensions;

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(Class<T> type, String name, Supplier<T> extensionSupplier) {
        T extension = (T) extensions.findByName(name);
        if (extension == null) {
            log.info("Extension [{}] not found, creating new instance", name);
            extension = extensionSupplier.get();
            extensions.add(type, name, extension);
        } else {
            log.info("Extension [{}] found, using existing instance", name);
        }
        return extension;
    }
}
