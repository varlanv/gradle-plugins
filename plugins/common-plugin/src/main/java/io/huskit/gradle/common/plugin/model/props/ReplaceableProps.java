package io.huskit.gradle.common.plugin.model.props;

import io.huskit.gradle.common.plugin.model.NewOrExistingExtension;
import lombok.RequiredArgsConstructor;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.provider.ProviderFactory;

@RequiredArgsConstructor
public class ReplaceableProps implements Props {

    private final ProviderFactory providers;
    private final ExtensionContainer extensions;
    private Props delegate;

    @Override
    public boolean hasProp(String name) {
        return getDelegate().hasProp(name);
    }

    @Override
    public NonNullProp nonnull(String name) {
        return getDelegate().nonnull(name);
    }

    @Override
    public NullableProp nullable(String name) {
        return getDelegate().nullable(name);
    }

    @Override
    public NullableProp env(String name) {
        return getDelegate().env(name);
    }

    private Props getDelegate() {
        if (delegate == null) {
            delegate = new NewOrExistingExtension(extensions).getOrCreate(
                    Props.class,
                    Props.EXTENSION_NAME,
                    () -> new DefaultProps(
                            providers,
                            extensions.getExtraProperties()
                    ));
        }
        return delegate;
    }
}
