package io.huskit.containers.http;

import io.huskit.common.Mutable;
import io.huskit.common.number.Hexadecimal;
import lombok.experimental.NonFinal;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

final class PushMultiplexedStream implements PushResponse<MultiplexedFrames> {

    StreamType streamType;
    List<MultiplexedFrame> frameList = new ArrayList<>();
    Mutable<MultiplexedFrames> response = Mutable.of();
    Mutable<Predicate<MultiplexedFrame>> follow = Mutable.of();
    @NonFinal
    boolean isChunkSizePart = true;
    @NonFinal
    int currentFrameHeadIndex = 0;
    @NonFinal
    int currentFrameSize = 0;
    @NonFinal
    boolean isStreamFrameHeaderPart = true;
    @NonFinal
    byte[] currentFrameBuffer;
    @NonFinal
    FrameType currentFrameType;
    @NonFinal
    Hexadecimal currentChunkSizeHex = Hexadecimal.fromHexChars();
    @NonFinal
    int skipNext = 0;

    PushMultiplexedStream(StreamType streamType, Predicate<MultiplexedFrame> follow) {
        this.streamType = Objects.requireNonNull(streamType);
        this.follow.set(follow);
    }

    PushMultiplexedStream(StreamType streamType) {
        this.streamType = Objects.requireNonNull(streamType);
    }

    PushMultiplexedStream() {
        this(StreamType.ALL);
    }

    @Override
    public boolean isReady() {
        return response.isPresent();
    }

    @Override
    public MultiplexedFrames value() {
        return response.require();
    }

    @Override
    public synchronized Optional<MultiplexedFrames> apply(ByteBuffer byteBuffer) {
        if (response.isPresent()) {
            return response.maybe();
        }
        while (byteBuffer.hasRemaining()) {
            var currentByte = byteBuffer.get();
            if (skipNext > 0) {
                skipNext--;
                continue;
            }
            if (isChunkSizePart) {
                if (currentByte != '\r') {
                    if (currentByte == '\n') {
                        isChunkSizePart = false;
                        isStreamFrameHeaderPart = true;
                        if (currentChunkSizeHex.intValue() == 0) {
                            response.set(new MultiplexedFrames(frameList));
                            return Optional.of(response.require());
                        }
                    } else {
                        currentChunkSizeHex = currentChunkSizeHex.withHexChar((char) currentByte);
                    }
                }
            } else {
                if (isStreamFrameHeaderPart) {
                    if (currentFrameHeadIndex == 0) {
                        if (currentByte == 1) {
                            currentFrameType = FrameType.STDOUT;
                        } else if (currentByte == 2) {
                            currentFrameType = FrameType.STDERR;
                        } else {
                            throw new IllegalStateException("Invalid frame type: " + currentByte);
                        }
                        currentFrameHeadIndex++;
                    } else if (currentFrameHeadIndex >= 1 && currentFrameHeadIndex <= 3) {
                        currentFrameHeadIndex++;
                    } else {
                        if (currentFrameHeadIndex == 4) {
                            currentFrameSize |= Math.toIntExact((currentByte & 0xFFL) << 24);
                            currentFrameHeadIndex++;
                        } else if (currentFrameHeadIndex == 5) {
                            currentFrameSize |= Math.toIntExact((currentByte & 0xFFL) << 16);
                            currentFrameHeadIndex++;
                        } else if (currentFrameHeadIndex == 6) {
                            currentFrameSize |= Math.toIntExact((currentByte & 0xFFL) << 8);
                            currentFrameHeadIndex++;
                        } else if (currentFrameHeadIndex == 7) {
                            currentFrameSize |= Math.toIntExact(currentByte & 0xFFL);
                            currentFrameBuffer = new byte[currentFrameSize];
                            currentFrameHeadIndex = 0;
                            isStreamFrameHeaderPart = false;
                        }
                    }
                } else {
                    if (currentFrameSize > 1) {
                        currentFrameBuffer[currentFrameBuffer.length - currentFrameSize] = currentByte;
                    } else {
                        currentFrameBuffer[currentFrameBuffer.length - currentFrameSize] = currentByte;
                        var frame = new MultiplexedFrame(currentFrameBuffer, currentFrameType);
                        if (streamType == StreamType.ALL || (currentFrameType == FrameType.STDERR && streamType == StreamType.STDERR) ||
                            (currentFrameType == FrameType.STDOUT && streamType == StreamType.STDOUT)) {
                            frameList.add(frame);
                            if (follow.isPresent()) {
                                if (follow.require().test(frame)) {
                                    response.set(new MultiplexedFrames(frameList));
                                    return Optional.of(response.require());
                                } else {
                                    isStreamFrameHeaderPart = true;
                                }
                            } else {
                                response.set(new MultiplexedFrames(frameList));
                                isStreamFrameHeaderPart = true;
                            }
                        }
                    }
                    currentFrameSize--;
                }
                currentChunkSizeHex.decrement();
                if (currentChunkSizeHex.intValue() == 0) {
                    isChunkSizePart = true;
                    isStreamFrameHeaderPart = true;
                    skipNext = 2;
                }
            }
        }
        return Optional.empty();
    }
}