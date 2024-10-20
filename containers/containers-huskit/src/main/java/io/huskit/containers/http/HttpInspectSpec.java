package io.huskit.containers.http;

final class HttpInspectSpec implements HtUrl {

    String id;

    public HttpInspectSpec(CharSequence id) {
        this.id = id.toString();
    }

    @Override
    public String url() {
        return "/containers/" + id + "/json";
    }
}
