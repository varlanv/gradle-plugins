package io.huskit.common.io;

public interface Lines {

    Line next();

    static Lines fromIterable(Iterable<String> iterable) {
        var iterator = iterable.iterator();
        return () -> iterator.hasNext() ? new Line(iterator.next()) : new Line();
    }
}
