package com.nongthinh.post_service.domain.post.valueobject;

public enum MediaType {
    IMAGE("image/"),
    VIDEO("video/");

    private final String contentTypePrefix;

    MediaType(String contentTypePrefix) {
        this.contentTypePrefix = contentTypePrefix;
    }

    public boolean accepts(String contentType) {
        return contentType != null && contentType.startsWith(contentTypePrefix);
    }
}
