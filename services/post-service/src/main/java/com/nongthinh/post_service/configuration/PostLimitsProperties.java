package com.nongthinh.post_service.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "post.limits")
public record PostLimitsProperties(int contentLength, int mediaPerPost, int pageSize) {
    public PostLimitsProperties {
        if (contentLength < 1 || contentLength > 2_000) {
            throw new IllegalArgumentException("post.limits.content-length must be between 1 and 2000");
        }
        if (mediaPerPost < 0 || mediaPerPost > 100) {
            throw new IllegalArgumentException("post.limits.media-per-post must be between 0 and 100");
        }
        if (pageSize < 1 || pageSize > 500) {
            throw new IllegalArgumentException("post.limits.page-size must be between 1 and 500");
        }
    }
}
