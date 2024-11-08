package io.huskit.containers.http;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public final class MultiplexedFrames {

    List<MultiplexedFrame> frames;

    @Override
    public String toString() {
        return "MultiplexedFrames{" +
            "frames=" + frames +
            '}';
    }

    public List<MultiplexedFrame> list() {
        return Collections.unmodifiableList(frames);
    }

    public Stream<MultiplexedFrame> stream() {
        return frames.stream();
    }

    public Stream<String> allLines() {
        return stream()
            .map(MultiplexedFrame::stringData);
    }

    public Stream<String> stdOut() {
        return stream()
            .filter(frame -> frame.type() == FrameType.STDOUT)
            .map(MultiplexedFrame::stringData);
    }

    public Stream<String> stdErr() {
        return stream()
            .filter(frame -> frame.type() == FrameType.STDERR)
            .map(MultiplexedFrame::stringData);
    }
}
