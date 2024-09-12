package io.huskit.containers.api.logs;

class LookForNothing implements LookFor {

    static final LookForNothing INSTANCE = new LookForNothing();

    @Override
    public String value() {
        throw new UnsupportedOperationException("Should not be called");
    }
}
