package com.nongthinh.post_service.application.view;

import java.util.UUID;

public record PostBookmarkStatusView(UUID postId, boolean bookmarked) {
}
