package com.nongthinh.post_service.infra.client.profile;

import java.util.List;
import java.util.UUID;

public record FollowingPageDto(List<Profile> items, int page, boolean hasNext) {
    public record Profile(UUID userId) { }
}
