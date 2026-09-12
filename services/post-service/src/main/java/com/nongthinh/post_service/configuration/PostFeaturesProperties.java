package com.nongthinh.post_service.configuration;

import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "post.features")
public record PostFeaturesProperties(
        Set<PostVisibility> allowedVisibility,
        Set<MediaType> allowedMediaType
) {
    public PostFeaturesProperties {
        if (allowedVisibility == null || allowedVisibility.isEmpty()) {
            throw new IllegalArgumentException("post.features.allowed-visibility must not be empty");
        }
        if (allowedMediaType == null || allowedMediaType.isEmpty()) {
            throw new IllegalArgumentException("post.features.allowed-media-type must not be empty");
        }
        allowedVisibility = Set.copyOf(allowedVisibility);
        allowedMediaType = Set.copyOf(allowedMediaType);
    }
}
