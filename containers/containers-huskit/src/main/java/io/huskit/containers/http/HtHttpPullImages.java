package io.huskit.containers.http;

import io.huskit.common.concurrent.FinishFuture;
import io.huskit.containers.api.image.HtPullImages;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class HtHttpPullImages implements HtPullImages {

    HtHttpDockerSpec dockerSpec;
    HtHttpPullImagesSpec pullImagesSpec;

    @Override
    public void exec() {
        FinishFuture.finish(
            dockerSpec.socket().sendPushAsync(
                new PushRequest<>(
                    new Request(
                        dockerSpec.requests().post(pullImagesSpec)
                    ).withExpectedStatus(200),
                    new PushRaw()
                )
            ),
            dockerSpec.defaultTimeout()
        );
    }
}
