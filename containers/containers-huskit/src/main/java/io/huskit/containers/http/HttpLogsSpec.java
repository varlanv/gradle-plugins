package io.huskit.containers.http;

import io.huskit.common.Mutable;

final class HttpLogsSpec implements HtUrl {

    String containerId;
    Mutable<Boolean> follow = Mutable.of(false);

    HttpLogsSpec(CharSequence containerId) {
        this.containerId = containerId.toString();
    }

    public HttpLogsSpec withFollow(Boolean follow) {
        this.follow.set(follow);
        return this;
    }

    @Override
    public String url() {
        if (follow.require()) {
            return "/containers/" + containerId + "/logs?stdout=true&stderr=true&follow=true";
        } else {
            return "/containers/" + containerId + "/logs?stdout=true&stderr=true";
        }
    }
}
