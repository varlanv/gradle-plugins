package io.huskit.common;

import io.huskit.common.function.MemoizedSupplier;

import java.util.Objects;
import java.util.function.Supplier;

public class FakeTestLog implements Log {

    Log delegate;

    public FakeTestLog() {
        this.delegate = new ConditionalLog(
                new StdLog(),
                MemoizedSupplier.of(() ->
                        Objects.equals(
                                System.getProperty(HtConstants.TEST_SYSTEM_PROPERTY),
                                "true"
                        )
                )
        );
    }

    @Override
    public void debug(Supplier<String> message) {
        delegate.debug(message);
    }
}
