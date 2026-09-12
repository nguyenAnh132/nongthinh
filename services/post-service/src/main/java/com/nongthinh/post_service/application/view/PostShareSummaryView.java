package com.nongthinh.post_service.application.view;

import java.util.UUID;

public record PostShareSummaryView(UUID postId, long totalCount) {
}
